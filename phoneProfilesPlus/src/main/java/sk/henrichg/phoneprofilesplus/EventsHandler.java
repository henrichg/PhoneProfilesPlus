package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.telephony.TelephonyManager;

//import com.crashlytics.android.Crashlytics;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

class EventsHandler {
    
    final Context context;

    String sensorType;

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
    private String eventAlarmClockPackageName;
    private long eventDeviceBootDate;

    private boolean startProfileMerged;
    private boolean endProfileMerged;

    boolean notAllowedTime;
    boolean notAllowedBattery;
    boolean notAllowedCall;
    boolean notAllowedPeripheral;
    boolean notAllowedCalendar;
    boolean notAllowedWifi;
    boolean notAllowedScreen;
    boolean notAllowedBluetooth;
    boolean notAllowedSms;
    boolean notAllowedNotification;
    boolean notAllowedApplication;
    boolean notAllowedLocation;
    boolean notAllowedOrientation;
    boolean notAllowedMobileCell;
    boolean notAllowedNfc;
    boolean notAllowedRadioSwitch;
    boolean notAllowedAlarmClock;
    boolean notAllowedDeviceBoot;

    boolean timePassed;
    boolean batteryPassed;
    boolean callPassed;
    boolean peripheralPassed;
    boolean calendarPassed;
    boolean wifiPassed;
    boolean screenPassed;
    boolean bluetoothPassed;
    boolean smsPassed;
    boolean notificationPassed;
    boolean applicationPassed;
    boolean locationPassed;
    boolean orientationPassed;
    boolean mobileCellPassed;
    boolean nfcPassed;
    boolean radioSwitchPassed;
    boolean alarmClockPassed;
    boolean deviceBootPassed;


    static final String SENSOR_TYPE_RADIO_SWITCH = "radioSwitch";
    static final String SENSOR_TYPE_RESTART_EVENTS = "restartEvents";
    static final String SENSOR_TYPE_RESTART_EVENTS_NOT_UNBLOCK = "restartEventsNotUnblock";
    static final String SENSOR_TYPE_MANUAL_RESTART_EVENTS = "manualRestartEvents";
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
    static final String SENSOR_TYPE_DEVICE_BOOT = "deviceBoot";
    static final String SENSOR_TYPE_DEVICE_BOOT_EVENT_END = "deviceBootEventEnd";

    public EventsHandler(Context context) {
        this.context = context.getApplicationContext();
    }
    
    void handleEvents(String sensorType) {
        synchronized (PPApplication.eventsHandlerMutex) {
            //CallsCounter.logCounter(context, "EventsHandler.handleEvents", "EventsHandler_handleEvents");

            //PPApplication.logE("[TEST BATTERY] EventsHandler.handleEvents", "-- start --------------------------------");

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
            //PPApplication.logE("[TEST BATTERY] EventsHandler.handleEvents", "sensorType=" + this.sensorType);
            //CallsCounter.logCounterNoInc(context, "EventsHandler.handleEvents->sensorType=" + this.sensorType, "EventsHandler_handleEvents");

            //restartAtEndOfEvent = false;

            // disabled for firstStartEvents
            //if (!PPApplication.getApplicationStarted(context))
            // application is not started
            //	return;

            //PPApplication.setApplicationStarted(context, true);

            DataWrapper dataWrapper = new DataWrapper(context.getApplicationContext(), false, 0, false);
            dataWrapper.fillEventList();

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

                doEndHandler(dataWrapper);
                //dataWrapper.invalidateDataWrapper();

                //PPApplication.logE("[TEST BATTERY] EventsHandler.handleEvents", "-- end: events globally stopped --------------------------------");

                return;
            }

            /*
            // start orientation listener only when events exists
            if (ppService != null) {
                if (!PhoneProfilesService.isOrientationScannerStarted()) {
                    int eventCount = 0;
                    for (Event _event : dataWrapper.eventList) {
                        if ((_event.getStatus() != Event.ESTATUS_STOP) &&
                                (_event._eventPreferencesOrientation._enabled))
                            eventCount++;
                    }
                    //if (dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_ORIENTATION) > 0)
                    //    PPApplication.startOrientationScanner(context);
                }
            }
            */

            if (!eventsExists(sensorType, dataWrapper)) {
                // events not exists

                doEndHandler(dataWrapper);
                //dataWrapper.invalidateDataWrapper();

                //PPApplication.logE("[TEST BATTERY] EventsHandler.handleEvents", "-- end: not events found --------------------------------");

                return;
            }

            //PPApplication.logE("#### EventsHandler.handleEvents", "do EventsHandler");

            boolean isRestart = sensorType.equals(SENSOR_TYPE_RESTART_EVENTS) || sensorType.equals(SENSOR_TYPE_MANUAL_RESTART_EVENTS);

            //interactive = (!isRestart) || _interactive;

            boolean saveCalendarStartEndTime = false;
            if (isRestart) {
                if (Event.isEventPreferenceAllowed(EventPreferencesCalendar.PREF_EVENT_CALENDAR_ENABLED, context.getApplicationContext()).allowed ==
                        PreferenceAllowed.PREFERENCE_ALLOWED) {
                    int eventCount = 0;
                    for (Event _event : dataWrapper.eventList) {
                        if ((_event.getStatus() != Event.ESTATUS_STOP) &&
                                (_event._eventPreferencesCalendar._enabled))
                            eventCount++;
                    }
                    //int eventCount = DatabaseHandler.getInstance(context.getApplicationContext())
                    //        .getTypeEventsCount(DatabaseHandler.ETYPE_CALENDAR, false);
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
                            _event._eventPreferencesTime.setSystemEventForPause(context);
                        if (_event.getStatus() == Event.ESTATUS_PAUSE)
                            _event._eventPreferencesTime.setSystemEventForStart(context);
                    }
                }
            }*/

            if (isRestart) {
                // for restart events, set startTime to 0
                dataWrapper.clearSensorsStartTime();
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
                                _event._eventPreferencesAlarmClock.saveStartTime(dataWrapper, eventAlarmClockDate, eventAlarmClockPackageName);
                            }
                        }
                    }
                }
                if (sensorType.equals(SENSOR_TYPE_DEVICE_BOOT)) {
                    // search for device boot events, save start time
                    //PPApplication.logE("EventsHandler.handleEvents", "search for device boot events");
                    for (Event _event : dataWrapper.eventList) {
                        if (_event.getStatus() != Event.ESTATUS_STOP) {
                            if (_event._eventPreferencesDeviceBoot._enabled) {
                                //PPApplication.logE("EventsHandler.handleEvents", "event._id=" + _event._id);
                                _event._eventPreferencesDeviceBoot.saveStartTime(dataWrapper, eventDeviceBootDate);
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
            PPApplication.lockRefresh = true;

            Profile mergedProfile = DataWrapper.getNonInitializedProfile("", "", 0);
            //Profile mergedPausedProfile = DataWrapper.getNonInitializedProfile("", "", 0);

            int mergedProfilesCount = 0;
            int usedEventsCount = 0;

            //Profile oldActivatedProfile = dataWrapper.getActivatedProfileFromDB(false, false);
            Profile oldActivatedProfile = Profile.getProfileFromSharedPreferences(context, PPApplication.ACTIVATED_PROFILE_PREFS_NAME);
            boolean profileChanged = false;

            //boolean activateProfileAtEnd = false;
            //boolean anyEventPaused = false;
            //Event notifyEventEnd = null;
            boolean notified = false;

            // do not reactivate profile, activate only changes
            //boolean reactivateProfile = false;

            /*
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
            */

            List<EventTimeline> eventTimelineList = dataWrapper.getEventTimelineList(false);

            if (isRestart) {
                /*if (PPApplication.logEnabled()) {
                    PPApplication.logE("$$$ EventsHandler.handleEvents", "restart events");
                    PPApplication.logE("[DEFPROF] EventsHandler.handleEvents", "restart events");
                }*/

                //reactivateProfile = true;

                //oldActivatedProfile = null;

                // 1. pause events
                sortEventsByStartOrderDesc(dataWrapper.eventList);
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
                        doHandleEvent(_event, true, true, /*interactive,*/ false, false, /*reactivateProfile,*/ mergedProfile, dataWrapper);
                        boolean paused = _event.getStatus() == Event.ESTATUS_PAUSE;

                        if (running && paused) {
                            if (startProfileMerged)
                                mergedProfilesCount++;
                            if (endProfileMerged)
                                mergedProfilesCount++;
                            if (startProfileMerged || endProfileMerged)
                                usedEventsCount++;

                            //anyEventPaused = true;
                            //notifyEventEnd = _event;
                            _event.notifyEventEnd(false);
                        }
                    }
                }

                //runningEventCountP = _etl.size();

                // 2. start events
                //dataWrapper.sortEventsByStartOrderAsc();
                Collections.reverse(dataWrapper.eventList);
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
                        doHandleEvent(_event, false, true, /*interactive,*/ false, false, /*reactivateProfile,*/ mergedProfile, dataWrapper);
                        boolean running = _event.getStatus() == Event.ESTATUS_RUNNING;

                        if (running && paused) {
                            if (startProfileMerged)
                                mergedProfilesCount++;
                            if (endProfileMerged)
                                mergedProfilesCount++;
                            if (startProfileMerged || endProfileMerged)
                                usedEventsCount++;

                            _event.notifyEventStart(context, false);
                        }
                    }
                }
            } else {
                //PPApplication.logE("[TEST BATTERY]  EventsHandler.handleEvents", "NO restart events");
                /*if (PPApplication.logEnabled()) {
                    PPApplication.logE("$$$ EventsHandler.handleEvents", "NO restart events");
                    PPApplication.logE("[DEFPROF] EventsHandler.handleEvents", "NO restart events");
                }*/

                //oldActivatedProfile = dataWrapper.getActivatedProfile();

                //activatedProfile0 = dataWrapper.getActivatedProfileFromDB();

                //1. pause events
                sortEventsByStartOrderDesc(dataWrapper.eventList);
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
                        doHandleEvent(_event, true, false, /*interactive,*/ forDelayStartAlarm, forDelayEndAlarm, /*reactivateProfile,*/ mergedProfile, dataWrapper);
                        boolean paused = _event.getStatus() == Event.ESTATUS_PAUSE;

                        if (running && paused) {
                            if (startProfileMerged)
                                mergedProfilesCount++;
                            if (endProfileMerged)
                                mergedProfilesCount++;
                            if (startProfileMerged || endProfileMerged)
                                usedEventsCount++;

                            //anyEventPaused = true;
                            //notifyEventEnd = _event;
                            if (_event.notifyEventEnd(!notified))
                                notified = true;

                            /*
                            if ((ppService != null) && (_event._atEndDo == Event.EATENDDO_RESTART_EVENTS)) {
                                //PPApplication.logE("[DEFPROF] EventsHandler.handleEvents", "has restart events=");
                                ppService.willBeDoRestartEvents = true;
                            }*/
                            //if ((!activateProfileAtEnd) && ((_event._atEndDo == Event.EATENDDO_UNDONE_PROFILE) || (_event._fkProfileEnd != Profile.PROFILE_NO_ACTIVATE)))
                            //    activateProfileAtEnd = true;

                            /*if (PPApplication.logEnabled()) {
                                if (ppService != null)
                                    PPApplication.logE("[DEFPROF] EventsHandler.handleEvents", "ppService.willBeDoRestartEvents=" + ppService.willBeDoRestartEvents);
                            }*/
                        }
                    }
                }

                //runningEventCountP = _etl.size();

                //2. start events
                //mergedProfile.copyProfile(mergedPausedProfile);
                //dataWrapper.sortEventsByStartOrderAsc();
                Collections.reverse(dataWrapper.eventList);
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
                        doHandleEvent(_event, false, false, /*interactive,*/ forDelayStartAlarm, forDelayEndAlarm, /*true*//*reactivateProfile,*/ mergedProfile, dataWrapper);
                        boolean running = _event.getStatus() == Event.ESTATUS_RUNNING;

                        if (running && paused) {
                            if (startProfileMerged)
                                mergedProfilesCount++;
                            if (endProfileMerged)
                                mergedProfilesCount++;
                            if (startProfileMerged || endProfileMerged)
                                usedEventsCount++;

                            if (_event.notifyEventStart(context, !notified))
                                notified = true;
                        }
                    }
                }
            }

            PPApplication.lockRefresh = false;

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
            int runningEventCountE = eventTimelineList.size();

            Profile activatedProfile = dataWrapper.getActivatedProfileFromDB(false, false);
            long defaultProfileId = Profile.PROFILE_NO_ACTIVATE;
            boolean notifyDefaultProfile = false;

            //boolean fullyStarted = false;
            //if (ppService != null)
            //    fullyStarted = ppService.getApplicationFullyStarted();

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
                    //if ((ppService != null) && (!ppService.willBeDoRestartEvents)) {
                        // activate default profile, only when will not be do restart events from paused events

                        //PPApplication.logE("[DEFPROF] EventsHandler.handleEvents", "no events running");
                        // no events running
                        defaultProfileId = ApplicationPreferences.applicationDefaultProfile;
                        //if (!fullyStarted)
                        //    defaultProfileId = Profile.PROFILE_NO_ACTIVATE;
                        if (defaultProfileId != Profile.PROFILE_NO_ACTIVATE) {
                            //PPApplication.logE("[DEFPROF] EventsHandler.handleEvents", "default profile is set");
                            long activatedProfileId = 0;
                            if (activatedProfile != null)
                                activatedProfileId = activatedProfile._id;

                            if (ApplicationPreferences.applicationDefaultProfileUsage) {
                                // do not activate default profile when not any event is paused and no any profile is activated
                                // for example for screen on/off broadcast, when no any event is running
                                //if (!anyEventPaused && (mergedProfile._id == 0) && (mergedPausedProfile._id == 0))
                                //    activateProfileAtEnd = true;

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
                                        (
                                        // (!activateProfileAtEnd || ((mergedProfile._id != 0) && (mergedPausedProfile._id == 0))) &&
                                        (activatedProfileId != defaultProfileId))
                                )
                                {
                                    notifyDefaultProfile = true;
                                    mergedProfile.mergeProfiles(defaultProfileId, dataWrapper/*, false*/);
                                    mergedProfilesCount++;

                                    //PPApplication.logE("[DEFPROF] EventsHandler.handleEvents", "activated default profile");
                                }
                            } else {
                                if ((activatedProfileId == 0) ||
                                        isRestart ||
                                        (activatedProfileId != defaultProfileId)) {
                                    notifyDefaultProfile = true;
                                    mergedProfile.mergeProfiles(defaultProfileId, dataWrapper/*, false*/);
                                    mergedProfilesCount++;
                                    //PPApplication.logE("[DEFPROF] EventsHandler.handleEvents", "activated default profile");
                                }
                            }
                        }
                    //}
                    //else
                    //if (ppService != null)
                    //    ppService.willBeDoRestartEvents = false;
                }
            } else {
                //PPApplication.logE("[DEFPROF] EventsHandler.handleEvents", "active profile is activated manually");
                // manual profile activation
                defaultProfileId = ApplicationPreferences.applicationDefaultProfile;
                //if (!fullyStarted)
                //    defaultProfileId = Profile.PROFILE_NO_ACTIVATE;
                if (defaultProfileId != Profile.PROFILE_NO_ACTIVATE) {
                    if (activatedProfile == null) {
                        // if not profile activated, activate Default profile
                        notifyDefaultProfile = true;
                        mergedProfile.mergeProfiles(defaultProfileId, dataWrapper/*, false*/);
                        mergedProfilesCount++;
                        //PPApplication.logE("[DEFPROF] EventsHandler.handleEvents", "activated default profile");
                    }
                }
            }
            ////////////////

            //Event notifyEventStart = null;
            String defaultProfileNotificationSound = "";
            boolean defaultProfileNotificationVibrate = false;

            if (/*(!isRestart) &&*/ (defaultProfileId != Profile.PROFILE_NO_ACTIVATE) && notifyDefaultProfile) {
                // only when activated is background profile, play event notification sound

                defaultProfileNotificationSound = ApplicationPreferences.applicationDefaultProfileNotificationSound;
                defaultProfileNotificationVibrate = ApplicationPreferences.applicationDefaultProfileNotificationVibrate;
            }

            /*if (PPApplication.logEnabled()) {
                PPApplication.logE("$$$ EventsHandler.handleEvents", "mergedProfile=" + mergedProfile);
                PPApplication.logE("$$$ EventsHandler.handleEvents", "mergedProfile._id=" + mergedProfile._id);
            }*/

            boolean doSleep = false;

            //PPApplication.logE("[TEST BATTERY]  EventsHandler.handleEvents", "mergedProfile._name="+mergedProfile._name);

            if (mergedProfile._id != 0) {
                // activate merged profile
                /*if (PPApplication.logEnabled()) {
                    PPApplication.logE("$$$ EventsHandler.handleEvents", "#### oldActivatedProfile-profileName=" + oldActivatedProfile._name);
                    PPApplication.logE("$$$ EventsHandler.handleEvents", "#### oldActivatedProfile-profileId=" + oldActivatedProfile._id);

                    PPApplication.logE("$$$ EventsHandler.handleEvents", "#### mergedProfile-profileName=" + mergedProfile._name);
                    PPApplication.logE("$$$ EventsHandler.handleEvents", "#### mergedProfile-profileId=" + mergedProfile._id);
                    //PPApplication.logE("$$$ EventsHandler.handleEvents", "#### mergedProfile-_volumeRingerMode=" + mergedProfile._volumeRingerMode);
                    //PPApplication.logE("$$$ EventsHandler.handleEvents", "#### mergedProfile-_volumeZenMode=" + mergedProfile._volumeZenMode);
                    //PPApplication.logE("$$$ EventsHandler.handleEvents", "#### mergedProfile-_volumeRingtone=" + mergedProfile._volumeRingtone);
                    //PPApplication.logE("$$$ EventsHandler.handleEvents", "#### mergedProfile-_volumeNotification=" + mergedProfile._volumeNotification);
                }*/
                DatabaseHandler.getInstance(context.getApplicationContext()).saveMergedProfile(mergedProfile);

                //if (mergedProfile._id != oldActivatedProfileId)
                if (!mergedProfile.compareProfile(oldActivatedProfile))
                    profileChanged = true;
                //PPApplication.logE("$$$ EventsHandler.handleEvents", "#### profileChanged=" + profileChanged);
                //PPApplication.logE("$$$ EventsHandler.handleEvents", "#### isRestart=" + isRestart);

                if (profileChanged || isRestart /*sensorType.equals(SENSOR_TYPE_MANUAL_RESTART_EVENTS)*/) {
                    // log only when merged profile is not the same as last activated
                    // or for manual restart events

                    PPApplication.addActivityLog(context, PPApplication.ALTYPE_MERGED_PROFILE_ACTIVATION, null,
                            DataWrapper.getProfileNameWithManualIndicatorAsString(mergedProfile, true, "", false, false, false, dataWrapper),
                            mergedProfile._icon, 0, mergedProfilesCount + " [" + usedEventsCount + "]");

                    dataWrapper.activateProfileFromEvent(mergedProfile._id, false, true, isRestart);
                    // wait for profile activation
                    doSleep = true;
                }
            } else {
                //if ((ppService != null) && (!ppService.willBeDoRestartEvents)) {
                    // update only when will not be do restart events from paused events
                    //PPApplication.logE("DataWrapper.updateNotificationAndWidgets", "from EventsHandler.handleEvents");
                PPApplication.updateNotificationAndWidgets(false, false, context);
                //}
            }

            /*
            PPApplication.logE("[NOTIFY] EventsHandler.handleEvents", "notifyEventStart=" + notifyEventStart);
            if (notifyEventStart != null)
                PPApplication.logE("[NOTIFY] EventsHandler.handleEvents", "notifyEventStart._name=" + notifyEventStart._name);
            PPApplication.logE("[NOTIFY] EventsHandler.handleEvents", "notifyEventEnd=" + notifyEventEnd);
            if (notifyEventEnd != null)
                PPApplication.logE("[NOTIFY] EventsHandler.handleEvents", "notifyEventEnd._name=" + notifyEventEnd._name);
            PPApplication.logE("[NOTIFY] EventsHandler.handleEvents", "defaultProfileNotificationSound=" + defaultProfileNotificationSound);

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
                if (!defaultProfileNotificationSound.isEmpty() || defaultProfileNotificationVibrate) {
                    if (ppService != null) {
                        ppService.playNotificationSound(defaultProfileNotificationSound, defaultProfileNotificationVibrate);
                        //PPApplication.logE("[NOTIFY] EventsHandler.handleEvents", "default profile notified");
                        notified = true;
                    }
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

            //PPApplication.logE("[TEST BATTERY] EventsHandler.handleEvents", "-- end --------------------------------");
        }
    }

    private boolean eventsExists(String sensorType, DataWrapper dataWrapper/*, boolean onlyRunning*/) {
        for (Event _event : dataWrapper.eventList) {
            /*boolean eventEnabled;
            if (onlyRunning)
                eventEnabled = _event.getStatus() == Event.ESTATUS_RUNNING;
            else
                eventEnabled = _event.getStatus() != Event.ESTATUS_STOP;*/
            if (_event.getStatus() != Event.ESTATUS_STOP) {
                boolean sensorEnabled;

                switch (sensorType) {
                    case SENSOR_TYPE_BATTERY:
                    case SENSOR_TYPE_POWER_SAVE_MODE:
                        //eventType = DatabaseHandler.ETYPE_BATTERY;
                        sensorEnabled = _event._eventPreferencesBattery._enabled;
                        break;
                    case SENSOR_TYPE_BLUETOOTH_CONNECTION:
                    case SENSOR_TYPE_BLUETOOTH_STATE:
                        //eventType = DatabaseHandler.ETYPE_BLUETOOTH_CONNECTED;
                        sensorEnabled = _event._eventPreferencesBluetooth._enabled;
                        sensorEnabled = sensorEnabled &&
                                ((_event._eventPreferencesBluetooth._connectionType == 0) ||
                                 (_event._eventPreferencesBluetooth._connectionType == 2));
                        break;
                    case SENSOR_TYPE_BLUETOOTH_SCANNER:
                        //eventType = DatabaseHandler.ETYPE_BLUETOOTH_NEARBY;
                        sensorEnabled = _event._eventPreferencesBluetooth._enabled;
                        sensorEnabled = sensorEnabled &&
                                ((_event._eventPreferencesBluetooth._connectionType == 1) ||
                                 (_event._eventPreferencesBluetooth._connectionType == 3));
                        break;
                    case SENSOR_TYPE_CALENDAR_PROVIDER_CHANGED:
                    case SENSOR_TYPE_CALENDAR:
                    case SENSOR_TYPE_SEARCH_CALENDAR_EVENTS:
                        //eventType = DatabaseHandler.ETYPE_CALENDAR;
                        sensorEnabled = _event._eventPreferencesCalendar._enabled;
                        break;
                    case SENSOR_TYPE_DOCK_CONNECTION:
                    case SENSOR_TYPE_HEADSET_CONNECTION:
                        //eventType = DatabaseHandler.ETYPE_PERIPHERAL;
                        sensorEnabled = _event._eventPreferencesPeripherals._enabled;
                        break;
                    case SENSOR_TYPE_TIME:
                        //eventType = DatabaseHandler.ETYPE_TIME;
                        sensorEnabled = _event._eventPreferencesTime._enabled;
                        break;
                    case SENSOR_TYPE_APPLICATION:
                        //eventType = DatabaseHandler.ETYPE_APPLICATION;
                        sensorEnabled = _event._eventPreferencesApplication._enabled;
                        break;
                    case SENSOR_TYPE_NOTIFICATION:
                        //eventType = DatabaseHandler.ETYPE_NOTIFICATION;
                        sensorEnabled = _event._eventPreferencesNotification._enabled;
                        break;
                    /*case SENSOR_TYPE_NOTIFICATION_EVENT_END:
                        eventType = DatabaseHandler.ETYPE_NOTIFICATION;
                        break;*/
                    case SENSOR_TYPE_PHONE_CALL:
                    case SENSOR_TYPE_PHONE_CALL_EVENT_END:
                        //eventType = DatabaseHandler.ETYPE_CALL;
                        sensorEnabled = _event._eventPreferencesCall._enabled;
                        break;
                    case SENSOR_TYPE_SMS:
                    case SENSOR_TYPE_SMS_EVENT_END:
                        //eventType = DatabaseHandler.ETYPE_SMS;
                        sensorEnabled = _event._eventPreferencesSMS._enabled;
                        break;
                    case SENSOR_TYPE_WIFI_CONNECTION:
                    case SENSOR_TYPE_WIFI_STATE:
                        //eventType = DatabaseHandler.ETYPE_WIFI_CONNECTED;
                        sensorEnabled = _event._eventPreferencesWifi._enabled;
                        sensorEnabled = sensorEnabled &&
                                ((_event._eventPreferencesWifi._connectionType == 0) ||
                                 (_event._eventPreferencesWifi._connectionType == 2));
                        break;
                    case SENSOR_TYPE_WIFI_SCANNER:
                        //eventType = DatabaseHandler.ETYPE_WIFI_NEARBY;
                        sensorEnabled = _event._eventPreferencesWifi._enabled;
                        sensorEnabled = sensorEnabled &&
                                ((_event._eventPreferencesWifi._connectionType == 1) ||
                                 (_event._eventPreferencesWifi._connectionType == 3));
                        break;
                    case SENSOR_TYPE_GEOFENCES_SCANNER:
                    case SENSOR_TYPE_LOCATION_MODE:
                        //eventType = DatabaseHandler.ETYPE_LOCATION;
                        sensorEnabled = _event._eventPreferencesLocation._enabled;
                        break;
                    case SENSOR_TYPE_DEVICE_ORIENTATION:
                        //eventType = DatabaseHandler.ETYPE_ORIENTATION;
                        sensorEnabled = _event._eventPreferencesOrientation._enabled;
                        break;
                    case SENSOR_TYPE_PHONE_STATE:
                        //eventType = DatabaseHandler.ETYPE_MOBILE_CELLS;
                        sensorEnabled = _event._eventPreferencesMobileCells._enabled;
                        break;
                    case SENSOR_TYPE_NFC_TAG:
                    case SENSOR_TYPE_NFC_EVENT_END:
                        //eventType = DatabaseHandler.ETYPE_NFC;
                        sensorEnabled = _event._eventPreferencesNFC._enabled;
                        break;
                    case SENSOR_TYPE_RADIO_SWITCH:
                        //eventType = DatabaseHandler.ETYPE_RADIO_SWITCH;
                        sensorEnabled = _event._eventPreferencesRadioSwitch._enabled;
                        break;
                    case SENSOR_TYPE_ALARM_CLOCK:
                    case SENSOR_TYPE_ALARM_CLOCK_EVENT_END:
                        //eventType = DatabaseHandler.ETYPE_ALARM_CLOCK;
                        sensorEnabled = _event._eventPreferencesAlarmClock._enabled;
                        break;
                    case SENSOR_TYPE_DEVICE_BOOT:
                    case SENSOR_TYPE_DEVICE_BOOT_EVENT_END:
                        //eventType = DatabaseHandler.ETYPE_DEVICE_BOOT;
                        sensorEnabled = _event._eventPreferencesDeviceBoot._enabled;
                        break;
                    case SENSOR_TYPE_SCREEN:
                        // call doEventService for all screen on/off changes
                        //eventType = DatabaseHandler.ETYPE_SCREEN;
                        //sensorEnabled = _event._eventPreferencesScreen._enabled;
                    case SENSOR_TYPE_RESTART_EVENTS:
                        //eventType = DatabaseHandler.ETYPE_???;
                    case SENSOR_TYPE_EVENT_DELAY_START:
                        //eventType = DatabaseHandler.ETYPE_????;
                    case SENSOR_TYPE_EVENT_DELAY_END:
                        //eventType = DatabaseHandler.ETYPE_????;
                    case SENSOR_TYPE_DEVICE_IDLE_MODE:
                        //eventType = DatabaseHandler.ETYPE_????;
                    default:
                        sensorEnabled = true;
                        break;
                }

                if (sensorEnabled)
                    return true;
            }
        }
        return false;
    }

    private void doEndHandler(DataWrapper dataWrapper) {
        //PPApplication.logE("EventsHandler.doEndHandler","sensorType="+sensorType);
        //PPApplication.logE("EventsHandler.doEndHandler","callEventType="+callEventType);

        if (sensorType.equals(SENSOR_TYPE_PHONE_CALL)) {
            TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

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
                            if (_event._eventPreferencesCall.isPhoneNumberConfigured(phoneNumber/*, dataWrapper*/)) {
                                simulateRingingCall = true;
                                break;
                            }
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
                } catch (Exception e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                    //Crashlytics.logException(e);
                }
            }

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

//--------

    @SuppressLint({ "NewApi", "SimpleDateFormat" })
    private void doHandleEvent(Event event, boolean statePause,
                               boolean forRestartEvents, /*boolean interactive,*/
                               boolean forDelayStartAlarm, boolean forDelayEndAlarm,
            /*boolean reactivate,*/ Profile mergedProfile, DataWrapper dataWrapper)
    {
        if (!EditorProfilesActivity.displayRedTextToPreferencesNotification(null, event, context)) {
            event.setStatus(Event.ESTATUS_STOP);
            return;
        }

        startProfileMerged = false;
        endProfileMerged = false;

        int newEventStatus;// = Event.ESTATUS_NONE;

        notAllowedTime = false;
        notAllowedBattery = false;
        notAllowedCall = false;
        notAllowedPeripheral = false;
        notAllowedCalendar = false;
        notAllowedWifi = false;
        notAllowedScreen = false;
        notAllowedBluetooth = false;
        notAllowedSms = false;
        notAllowedNotification = false;
        notAllowedApplication = false;
        notAllowedLocation = false;
        notAllowedOrientation = false;
        notAllowedMobileCell = false;
        notAllowedNfc = false;
        notAllowedRadioSwitch = false;
        notAllowedAlarmClock = false;
        notAllowedDeviceBoot = false;

        timePassed = true;
        batteryPassed = true;
        callPassed = true;
        peripheralPassed = true;
        calendarPassed = true;
        wifiPassed = true;
        screenPassed = true;
        bluetoothPassed = true;
        smsPassed = true;
        notificationPassed = true;
        applicationPassed = true;
        locationPassed = true;
        orientationPassed = true;
        mobileCellPassed = true;
        nfcPassed = true;
        radioSwitchPassed = true;
        alarmClockPassed = true;
        deviceBootPassed = true;

        /*if (PPApplication.logEnabled()) {
            PPApplication.logE("%%%%%%% EventsHandler.doHandleEvents", "--- start --------------------------");
            PPApplication.logE("%%%%%%% EventsHandler.doHandleEvents", "------- event._id=" + event._id);
            PPApplication.logE("%%%%%%% EventsHandler.doHandleEvents", "------- event._name=" + event._name);
            PPApplication.logE("%%%%%%% EventsHandler.doHandleEvents", "------- sensorType=" + sensorType);
        }*/

        event._eventPreferencesTime.doHandleEvent(this/*, forRestartEvents*/);
        event._eventPreferencesBattery.doHandleEvent(this/*, forRestartEvents*/);
        event._eventPreferencesCall.doHandleEvent(this/*, forRestartEvents*/);
        event._eventPreferencesPeripherals.doHandleEvent(this/*, forRestartEvents*/);
        event._eventPreferencesCalendar.doHandleEvent(this/*, forRestartEvents*/);
        event._eventPreferencesWifi.doHandleEvent(this, forRestartEvents);
        event._eventPreferencesScreen.doHandleEvent(this/*, forRestartEvents*/);
        event._eventPreferencesBluetooth.doHandleEvent(this, forRestartEvents);
        event._eventPreferencesSMS.doHandleEvent(this/*, forRestartEvents*/);
        event._eventPreferencesNotification.doHandleEvent(this/*, forRestartEvents*/);
        event._eventPreferencesApplication.doHandleEvent(this/*, forRestartEvents*/);
        event._eventPreferencesLocation.doHandleEvent(this, forRestartEvents);
        event._eventPreferencesOrientation.doHandleEvent(this, forRestartEvents);
        event._eventPreferencesMobileCells.doHandleEvent(this, forRestartEvents);
        event._eventPreferencesNFC.doHandleEvent(this/*, forRestartEvents*/);
        event._eventPreferencesRadioSwitch.doHandleEvent(this/*, forRestartEvents*/);
        event._eventPreferencesAlarmClock.doHandleEvent(this/*, forRestartEvents*/);
        event._eventPreferencesDeviceBoot.doHandleEvent(this/*, forRestartEvents*/);

        boolean allPassed = true;
        boolean someNotAllowed = false;
        if (!notAllowedTime)
            //noinspection ConstantConditions
            allPassed &= timePassed;
        else
            someNotAllowed = true;
        if (!notAllowedBattery)
            allPassed &= batteryPassed;
        else
            someNotAllowed = true;
        if (!notAllowedCall)
            allPassed &= callPassed;
        else
            someNotAllowed = true;
        if (!notAllowedPeripheral)
            allPassed &= peripheralPassed;
        else
            someNotAllowed = true;
        if (!notAllowedCalendar)
            allPassed &= calendarPassed;
        else
            someNotAllowed = true;
        if (!notAllowedWifi)
            allPassed &= wifiPassed;
        else
            someNotAllowed = true;
        if (!notAllowedScreen)
            allPassed &= screenPassed;
        else
            someNotAllowed = true;
        if (!notAllowedBluetooth)
            allPassed &= bluetoothPassed;
        else
            someNotAllowed = true;
        if (!notAllowedSms)
            allPassed &= smsPassed;
        else
            someNotAllowed = true;
        if (!notAllowedNotification)
            allPassed &= notificationPassed;
        else
            someNotAllowed = true;
        if (!notAllowedApplication)
            allPassed &= applicationPassed;
        else
            someNotAllowed = true;
        if (!notAllowedLocation)
            allPassed &= locationPassed;
        else
            someNotAllowed = true;
        if (!notAllowedOrientation)
            allPassed &= orientationPassed;
        else
            someNotAllowed = true;
        if (!notAllowedMobileCell)
            allPassed &= mobileCellPassed;
        else
            someNotAllowed = true;
        if (!notAllowedNfc)
            allPassed &= nfcPassed;
        else
            someNotAllowed = true;
        if (!notAllowedRadioSwitch)
            allPassed &= radioSwitchPassed;
        else
            someNotAllowed = true;
        if (!notAllowedAlarmClock)
            allPassed &= alarmClockPassed;
        else
            someNotAllowed = true;
        if (!notAllowedDeviceBoot)
            allPassed &= deviceBootPassed;
        else
            someNotAllowed = true;

        /*if (PPApplication.logEnabled()) {
            PPApplication.logE("EventsHandler.doHandleEvents", "timePassed=" + timePassed);
            PPApplication.logE("EventsHandler.doHandleEvents", "batteryPassed=" + batteryPassed);
            PPApplication.logE("EventsHandler.doHandleEvents", "callPassed=" + callPassed);
            PPApplication.logE("EventsHandler.doHandleEvents", "peripheralPassed=" + peripheralPassed);
            PPApplication.logE("EventsHandler.doHandleEvents", "calendarPassed=" + calendarPassed);
            if (event._name.equals("Doma"))
                PPApplication.logE("[WiFi] EventsHandler.doHandleEvents", "wifiPassed=" + wifiPassed);
            PPApplication.logE("EventsHandler.doHandleEvents", "screenPassed=" + screenPassed);
            PPApplication.logE("EventsHandler.doHandleEvents", "bluetoothPassed=" + bluetoothPassed);
            PPApplication.logE("EventsHandler.doHandleEvents", "smsPassed=" + smsPassed);
            PPApplication.logE("EventsHandler.doHandleEvents", "notificationPassed=" + notificationPassed);
            PPApplication.logE("EventsHandler.doHandleEvents", "applicationPassed=" + applicationPassed);
            PPApplication.logE("EventsHandler.doHandleEvents", "locationPassed=" + locationPassed);
            PPApplication.logE("EventsHandler.doHandleEvents", "orientationPassed=" + orientationPassed);
            PPApplication.logE("EventsHandler.doHandleEvents", "mobileCellPassed=" + mobileCellPassed);
            PPApplication.logE("EventsHandler.doHandleEvents", "nfcPassed=" + nfcPassed);
            PPApplication.logE("EventsHandler.doHandleEvents", "radioSwitchPassed=" + radioSwitchPassed);
            PPApplication.logE("EventsHandler.doHandleEvents", "alarmClockPassed=" + alarmClockPassed);

            PPApplication.logE("EventsHandler.doHandleEvents", "notAllowedTime=" + notAllowedTime);
            PPApplication.logE("EventsHandler.doHandleEvents", "notAllowedBattery=" + notAllowedBattery);
            PPApplication.logE("EventsHandler.doHandleEvents", "notAllowedCall=" + notAllowedCall);
            PPApplication.logE("EventsHandler.doHandleEvents", "notAllowedPeripheral=" + notAllowedPeripheral);
            PPApplication.logE("EventsHandler.doHandleEvents", "notAllowedCalendar=" + notAllowedCalendar);
            if (event._name.equals("Doma"))
                PPApplication.logE("[WiFi] EventsHandler.doHandleEvents", "notAllowedWifi=" + notAllowedWifi);
            PPApplication.logE("EventsHandler.doHandleEvents", "notAllowedScreen=" + notAllowedScreen);
            PPApplication.logE("EventsHandler.doHandleEvents", "notAllowedBluetooth=" + notAllowedBluetooth);
            PPApplication.logE("EventsHandler.doHandleEvents", "notAllowedSms=" + notAllowedSms);
            PPApplication.logE("EventsHandler.doHandleEvents", "notAllowedNotification=" + notAllowedNotification);
            PPApplication.logE("EventsHandler.doHandleEvents", "notAllowedApplication=" + notAllowedApplication);
            PPApplication.logE("EventsHandler.doHandleEvents", "notAllowedLocation=" + notAllowedLocation);
            PPApplication.logE("EventsHandler.doHandleEvents", "notAllowedOrientation=" + notAllowedOrientation);
            PPApplication.logE("EventsHandler.doHandleEvents", "notAllowedMobileCell=" + notAllowedMobileCell);
            PPApplication.logE("EventsHandler.doHandleEvents", "notAllowedNfc=" + notAllowedNfc);
            PPApplication.logE("EventsHandler.doHandleEvents", "notAllowedRadioSwitch=" + notAllowedRadioSwitch);
            PPApplication.logE("EventsHandler.doHandleEvents", "notAllowedAlarmClock=" + notAllowedAlarmClock);

            if (event._name.equals("Event")) {
                PPApplication.logE("[***] EventsHandler.doHandleEvents", "allPassed=" + allPassed);
                PPApplication.logE("[***] EventsHandler.doHandleEvents", "someNotAllowed=" + someNotAllowed);
            }

            if (event._name.equals("Event")) {
                //PPApplication.logE("EventsHandler.doHandleEvents","eventStart="+eventStart);
                PPApplication.logE("[***] EventsHandler.doHandleEvents", "forRestartEvents=" + forRestartEvents);
                PPApplication.logE("[***] EventsHandler.doHandleEvents", "statePause=" + statePause);
            }
        }*/

        if (!someNotAllowed) {
            // some sensor is not allowed, do not change event status

            if (allPassed) {
                // all sensors are passed

                //if (eventStart)
                newEventStatus = Event.ESTATUS_RUNNING;
                //else
                //    newEventStatus = Event.ESTATUS_PAUSE;

            } else
                newEventStatus = Event.ESTATUS_PAUSE;

            /*if (PPApplication.logEnabled()) {
                if (event._name.equals("Event")) {
                    PPApplication.logE("[***] EventsHandler.doHandleEvents", "event.getStatus()=" + event.getStatus());
                    PPApplication.logE("[***] EventsHandler.doHandleEvents", "newEventStatus=" + newEventStatus);
                }
            }*/

            //PPApplication.logE("@@@ EventsHandler.doHandleEvents","restartEvent="+restartEvent);

            if ((event.getStatus() != newEventStatus) || forRestartEvents || event._isInDelayStart || event._isInDelayEnd) {
                //if (event._name.equals("Event"))
                //    PPApplication.logE("[***] EventsHandler.doHandleEvents", " do new event status");

                if ((newEventStatus == Event.ESTATUS_RUNNING) && (!statePause)) {
                    // do start of events, all sensors are passed

                    /*if (PPApplication.logEnabled()) {
                        if (event._name.equals("Event")) {
                            PPApplication.logE("[***] EventsHandler.doHandleEvents", "start event");
                            PPApplication.logE("[***] EventsHandler.doHandleEvents", "event._name=" + event._name);
                        }
                    }*/

                    if (event._isInDelayEnd)
                        event.removeDelayEndAlarm(dataWrapper);
                    else {
                        if (!forDelayStartAlarm) {
                            // called not for delay alarm
                            /*if (forRestartEvents) {
                                event._isInDelayStart = false;
                            } else*/ {
                                if (!event._isInDelayStart) {
                                    // if not delay alarm is set, set it
                                    // this also set event._isInDelayStart
                                    event.setDelayStartAlarm(dataWrapper); // for start delay
                                }
                                if (event._isInDelayStart) {
                                    // if delay expires, start event
                                    // this also set event._isInDelayStart
                                    event.checkDelayStart(/*this*/);
                                }
                            }
                            //if (event._name.equals("Event"))
                            //    PPApplication.logE("[***] EventsHandler.doHandleEvents", "event._isInDelayStart=" + event._isInDelayStart);
                            if (!event._isInDelayStart) {
                                // no delay alarm is set
                                // start event
                                long oldMergedProfile = mergedProfile._id;
                                event.startEvent(dataWrapper, /*interactive,*/ forRestartEvents, mergedProfile);
                                startProfileMerged = oldMergedProfile != mergedProfile._id;
                                //if (event._name.equals("Event"))
                                //    PPApplication.logE("[***] EventsHandler.doHandleEvents", "mergedProfile._id=" + mergedProfile._id);
                            }
                        }
                        /*if (PPApplication.logEnabled()) {
                            PPApplication.logE("[DSTART] EventsHandler.doHandleEvents", "forDelayStartAlarm=" + forDelayStartAlarm);
                            PPApplication.logE("[DSTART] EventsHandler.doHandleEvents", "event._isInDelayStart=" + event._isInDelayStart);
                        }*/
                        if (forDelayStartAlarm && event._isInDelayStart) {
                            // called for delay alarm
                            // start event
                            long oldMergedProfile = mergedProfile._id;
                            event.startEvent(dataWrapper, /*interactive,*/ forRestartEvents, mergedProfile);
                            startProfileMerged = oldMergedProfile != mergedProfile._id;
                            //PPApplication.logE("[DSTART] EventsHandler.doHandleEvents", "mergedProfile=" + mergedProfile._name);
                        }
                    }
                } else if (((newEventStatus == Event.ESTATUS_PAUSE) || forRestartEvents) && statePause) {
                    // do end of events, some sensors are not passed
                    // when pausing and it is for restart events (forRestartEvent=true), force pause

                    if (newEventStatus == Event.ESTATUS_RUNNING) {
                        //event must be running, all sensors are passed
                        //noinspection ConstantConditions
                        if (!forRestartEvents)
                            // it is not restart event, do not pause this event
                            return;
                    }

                    /*if (PPApplication.logEnabled()) {
                        if (event._name.equals("Event")) {
                            PPApplication.logE("[***] EventsHandler.doHandleEvents", "pause event");
                            PPApplication.logE("[***] EventsHandler.doHandleEvents", "event._name=" + event._name);
                        }
                    }*/

                    if (event._isInDelayStart) {
                        //if (event._name.equals("Event"))
                        //    PPApplication.logE("[***] EventsHandler.doHandleEvents", "isInDelayStart");
                        event.removeDelayStartAlarm(dataWrapper);
                    }
                    else {
                        if (!forDelayEndAlarm) {
                            //if (event._name.equals("Event"))
                            //    PPApplication.logE("[***] EventsHandler.doHandleEvents", "!forDelayEndAlarm");
                            // called not for delay alarm
                            /*if (forRestartEvents) {
                                event._isInDelayEnd = false;
                            } else*/ {
                                if (!event._isInDelayEnd) {
                                    // if not delay alarm is set, set it
                                    // this also set event._isInDelayEnd
                                    event.setDelayEndAlarm(dataWrapper); // for end delay
                                }
                                if (event._isInDelayEnd) {
                                    // if delay expires, pause event
                                    // this also set event._isInDelayEnd
                                    event.checkDelayEnd(/*this*/);
                                }
                            }
                            if (!event._isInDelayEnd) {
                                // no delay alarm is set
                                // pause event
                                long oldMergedProfile = mergedProfile._id;
                                event.pauseEvent(dataWrapper, true, false,
                                        false, true, mergedProfile, !forRestartEvents, forRestartEvents);
                                endProfileMerged = oldMergedProfile != mergedProfile._id;
                            }
                        }

                        if (forDelayEndAlarm && event._isInDelayEnd) {
                            // called for delay alarm
                            // pause event
                            long oldMergedProfile = mergedProfile._id;
                            event.pauseEvent(dataWrapper, true, false,
                                    false, true, mergedProfile, !forRestartEvents, forRestartEvents);
                            endProfileMerged = oldMergedProfile != mergedProfile._id;
                        }
                    }
                }
            }
        }

        //PPApplication.logE("%%% EventsHandler.doHandleEvents","--- end --------------------------");
    }

//--------


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

    void setEventAlarmClockParameters(long date, String alarmPackageName) {
        eventAlarmClockDate = date;
        eventAlarmClockPackageName = alarmPackageName;
    }

    void setEventCallParameters(int callEventType, String phoneNumber, long eventTime) {
        EventPreferencesCall.setEventCallEventType(context, callEventType);
        EventPreferencesCall.setEventCallEventTime(context, eventTime);
        EventPreferencesCall.setEventCallPhoneNumber(context, phoneNumber);
    }

    void setEventDeviceBootParameters(long date) {
        eventDeviceBootDate = date;
    }

    /*
    void sortEventsByStartOrderAsc()
    {
        class PriorityComparator implements Comparator<Event> {
            public int compare(Event lhs, Event rhs) {
                int res = 0;
                if ((lhs != null) && (rhs != null))
                    res =  lhs._startOrder - rhs._startOrder;
                return res;
            }
        }

        synchronized (eventList) {
            fillEventList();
            Collections.sort(eventList, new PriorityComparator());
        }
    }
    */

    private void sortEventsByStartOrderDesc(List<Event> eventList)
    {
        class PriorityComparator implements Comparator<Event> {
            public int compare(Event lhs, Event rhs) {
                int res = 0;
                if ((lhs != null) && (rhs != null))
                    res =  rhs._startOrder - lhs._startOrder;
                return res;
            }
        }

        Collections.sort(eventList, new PriorityComparator());
    }

}
