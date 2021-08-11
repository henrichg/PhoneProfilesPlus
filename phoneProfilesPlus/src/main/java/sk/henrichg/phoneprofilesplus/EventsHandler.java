package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

class EventsHandler {

    final Context context;

    String sensorType;

    private int oldRingerMode;
    //private int oldSystemRingerMode;
    private int oldZenMode;

    private String oldRingtone;
    private String oldRingtoneSIM1;
    private String oldRingtoneSIM2;

    //private String oldNotificationTone;
    //private int oldSystemRingerVolume;

    private String eventSMSPhoneNumber;
    private long eventSMSDate;
    private int eventSMSFromSIMSlot;
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
    boolean notAllowedAccessory;
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
    boolean notAllowedSoundProfile;

    boolean timePassed;
    boolean batteryPassed;
    boolean callPassed;
    boolean accessoryPassed;
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
    boolean soundProfilePassed;


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
    static final String SENSOR_TYPE_BATTERY_WITH_LEVEL = "batteryWithLevel";
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
    static final String SENSOR_TYPE_LOCATION_SCANNER = "locationScanner";
    static final String SENSOR_TYPE_LOCATION_MODE = "locationMode";
    static final String SENSOR_TYPE_DEVICE_ORIENTATION = "deviceOrientation";
    static final String SENSOR_TYPE_MOBILE_CELLS = "mobileCells";
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
    static final String SENSOR_TYPE_PERIODIC_EVENTS_HANDLER = "periodicEventsHandler";
    static final String SENSOR_TYPE_ACCESSORIES = "accessories";
    static final String SENSOR_TYPE_CALENDAR_EVENT_EXISTS_CHECK = "calendarEventExistsCheck";
    static final String SENSOR_TYPE_CONTACTS_CACHE_CHANGED = "contactsCacheChanged";
    static final String SENSOR_TYPE_SOUND_PROFILE = "soundProfile";
    static final String SENSOR_TYPE_ALL = "ALL";

    public EventsHandler(Context context) {
        this.context = context.getApplicationContext();
    }

    void handleEvents(String sensorType) {
        synchronized (PPApplication.eventsHandlerMutex) {
            //CallsCounter.logCounter(context, "EventsHandler.handleEvents", "EventsHandler_handleEvents");

//            PPApplication.logE("[APP_START] EventsHandler.handleEvents", "sensorType="+sensorType);

            boolean manualRestart = sensorType.equals(SENSOR_TYPE_MANUAL_RESTART_EVENTS);
            boolean isRestart = sensorType.equals(SENSOR_TYPE_RESTART_EVENTS) || manualRestart;

//            if (isRestart)
//                PPApplication.logE("[FIFO_TEST] EventsHandler.handleEvents", "-- start --------------------------------");

            if (!PPApplication.getApplicationStarted(true))
                // application is not started
                return;

//            if (isRestart)
//                PPApplication.logE("[FIFO_TEST] EventsHandler.handleEvents", "-- application started --------------------------------");

            PhoneProfilesService ppService;

            if (PhoneProfilesService.getInstance() != null) {
                ppService = PhoneProfilesService.getInstance();
            }
            else
                return;

            this.sensorType = sensorType;

//            PPApplication.logE("[IN_EVENTS_HANDLER] EventsHandler.handleEvents", "------ do EventsHandler, sensorType="+sensorType+" ------");
//            if (isRestart)
//                PPApplication.logE("[FIFO_TEST] EventsHandler.handleEvents", "------ do EventsHandler, sensorType="+sensorType+" ------");

            //boolean interactive;

            // save ringer mode, zen mode, ringtone before handle events
            // used by ringing call simulation (in doEndHandler())
            oldRingerMode = ApplicationPreferences.prefRingerMode;
            oldZenMode = ApplicationPreferences.prefZenMode;
            /*final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            if (audioManager != null) {
                oldSystemRingerMode = audioManager.getRingerMode();
                oldSystemRingerVolume = audioManager.getStreamVolume(AudioManager.STREAM_RING);
            }*/
            try {
                oldRingtone = "";
                oldRingtoneSIM1 = "";
                oldRingtoneSIM2 = "";

                Uri uri = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE);
                if (uri != null)
                    oldRingtone = uri.toString();

                Context appContext = context.getApplicationContext();
                final TelephonyManager telephonyManager = (TelephonyManager) appContext.getSystemService(Context.TELEPHONY_SERVICE);
                if (telephonyManager != null) {
                    int phoneCount = telephonyManager.getPhoneCount();
                    if (phoneCount > 1) {
                        if (PPApplication.deviceIsSamsung) {
                            String _uri = Settings.System.getString(appContext.getContentResolver(), "ringtone");
                            if (_uri != null)
                                oldRingtoneSIM1 = _uri;
                            else
                                oldRingtoneSIM1 = oldRingtone;
                            _uri = Settings.System.getString(appContext.getContentResolver(), "ringtone_2");
                            if (_uri != null)
                                oldRingtoneSIM2 = _uri;
                            else
                                oldRingtoneSIM2 = oldRingtone;
                        } else if (PPApplication.deviceIsHuawei && (PPApplication.romIsEMUI)) {
                            String _uri = Settings.System.getString(appContext.getContentResolver(), "ringtone");
                            if (_uri != null)
                                oldRingtoneSIM1 = _uri;
                            else
                                oldRingtoneSIM1 = oldRingtone;
                            _uri = Settings.System.getString(appContext.getContentResolver(), "ringtone2");
                            if (_uri != null)
                                oldRingtoneSIM2 = _uri;
                            else
                                oldRingtoneSIM2 = oldRingtone;
                        } else if (PPApplication.deviceIsXiaomi && (PPApplication.romIsMIUI)) {
                            String _uri = Settings.System.getString(appContext.getContentResolver(), "ringtone_sound_slot_1");
                            if (_uri != null)
                                oldRingtoneSIM1 = _uri;
                            else
                                oldRingtoneSIM1 = oldRingtone;

                            int useUniform = Settings.System.getInt(appContext.getContentResolver(), "ringtone_sound_use_uniform", 1);
                            if (useUniform == 0) {
                                _uri = Settings.System.getString(appContext.getContentResolver(), "ringtone_sound_slot_2");
                                if (_uri != null)
                                    oldRingtoneSIM2 = _uri;
                                else
                                    oldRingtoneSIM2 = oldRingtone;
                            }
                            else
                                oldRingtoneSIM2 = oldRingtoneSIM1;
                        }
                    }
                }
            } catch (SecurityException e) {
                Permissions.grantPlayRingtoneNotificationPermissions(context, false);
                oldRingtone = "";
                oldRingtoneSIM1 = "";
                oldRingtoneSIM2 = "";
            } catch (Exception e) {
                oldRingtone = "";
                oldRingtoneSIM1 = "";
                oldRingtoneSIM2 = "";
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

            if (!Event.getGlobalEventsRunning()) {
                // events are globally stopped

                doEndHandler(null, null);
                //dataWrapper.invalidateDataWrapper();

//                if (isRestart)
//                    PPApplication.logE("[FIFO_TEST] EventsHandler.handleEvents", "-- end: events globally stopped --------------------------------");

                return;
            }

//            PPApplication.logE("[APP_START] EventsHandler.handleEvents", "continue (1)");

            //PPApplication.logE("[TEST BATTERY] EventsHandler.handleEvents", "sensorType=" + this.sensorType);
            //CallsCounter.logCounterNoInc(context, "EventsHandler.handleEvents->sensorType=" + this.sensorType, "EventsHandler_handleEvents");

            if (DatabaseHandler.getInstance(context.getApplicationContext()).getNotStoppedEventsCount() == 0) {
                // not any event is paused or running
//                PPApplication.logE("[APP_START] EventsHandler.handleEvents", "setApplicationFullyStarted (01)");
                PPApplication.setApplicationFullyStarted(context);

                doEndHandler(null, null);

                return;
            }

            if (!alwaysEnabledSensors(sensorType)) {
                int eventType = getEventTypeForSensor(sensorType);
                if (DatabaseHandler.getInstance(context.getApplicationContext()).getTypeEventsCount(eventType/*, false*/) == 0) {
                    // events not exists

//                    PPApplication.logE("[EVENTS_HANDLER] EventsHandler.handleEvents", "------ events not exists ------");
//                    if (isRestart)
//                        PPApplication.logE("[FIFO_TEST] EventsHandler.handleEvents", "------ events not exists ------");

//                    PPApplication.logE("[APP_START] EventsHandler.handleEvents", "setApplicationFullyStarted (02)");
                    PPApplication.setApplicationFullyStarted(context);

                    doEndHandler(null, null);
                    //dataWrapper.invalidateDataWrapper();

                    /*
                    if (!DataWrapper.getIsManualProfileActivation(false, context)) {
                        // not manual profile activation
                        //PPApplication.logE("$$$ EventsHandler.handleEvents", "PPApplication.prefLastActivatedProfile="+PPApplication.prefLastActivatedProfile);
                        if (DatabaseHandler.getInstance(context).getCountEventsInTimeline() == 0) {
                            // not any event is running
                            //PPApplication.logE("$$$ EventsHandler.handleEvents", "not any event is running");

                            //if (isRestart) {
                                PPApplication.logE("[FIFO_TEST]  EventsHandler.handleEvents", "(1)");
                                PPApplication.logE("[FIFO_TEST]  EventsHandler.handleEvents", "sensorType=" + sensorType);
                                PPApplication.logE("[FIFO_TEST]  EventsHandler.handleEvents", "PPApplication.prefLastActivatedProfile=" + PPApplication.prefLastActivatedProfile);
                                PPApplication.logE("[FIFO_TEST]  EventsHandler.handleEvents", "runningEventCountE=0");
                            //}

                            if (PPApplication.prefLastActivatedProfile != 0) {
                                // last activated profile is not 0
                                PPApplication.setLastActivatedProfile(context, 0);
                                //PPApplication.logE("###### PPApplication.updateGUI", "from=EventsHandler.handleEvents (1)");
                                PPApplication.updateGUI(1); //context, true, true);
                            }
                        }
                    }
                    */

                    //if (isRestart) {
                    //    PPApplication.logE("###### PPApplication.updateGUI", "from=EventsHandler.handleEvents (1)");
                    //    PPApplication.updateGUI(/*context, true, true*/);
                    //}
                    //else {
                    //    PPApplication.logE("###### PPApplication.updateGUI", "from=EventsHandler.handleEvents (2)");
                    //    PPApplication.updateGUI(/*context, true, false*/);
                    //}

//                    if (isRestart)
//                        PPApplication.logE("[FIFO_TEST] EventsHandler.handleEvents", "-- end: not events found --------------------------------");

                    return;
                }
            }

//            PPApplication.logE("[APP_START] EventsHandler.handleEvents", "continue (2)");

            //restartAtEndOfEvent = false;

            DataWrapper dataWrapper = new DataWrapper(context.getApplicationContext(), false, 0, false, 0, 0f);
            dataWrapper.fillEventList();
            dataWrapper.fillEventTimelineList();
            dataWrapper.fillProfileList(false, false);

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
                    sensorType.equals(SENSOR_TYPE_CALENDAR_EVENT_EXISTS_CHECK) ||
                    sensorType.equals(SENSOR_TYPE_SCREEN) || // also for screen on/off compute calendar alarms
                    saveCalendarStartEndTime) {
                // search for calendar events
                //PPApplication.logE("[CALENDAR] EventsHandler.handleEvents", "search for calendar events");
                for (Event _event : dataWrapper.eventList) {
                    if ((_event._eventPreferencesCalendar._enabled) && (_event.getStatus() != Event.ESTATUS_STOP)) {
                        if (_event._eventPreferencesCalendar.isRunnable(context)) {
                            //PPApplication.logE("[CALENDAR] EventsHandler.handleEvents", "event._id=" + _event._id);
                            _event._eventPreferencesCalendar.saveStartEndTime(dataWrapper);
                            _event._eventPreferencesCalendar.saveCalendarEventExists(dataWrapper);
                        }
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
                if (sensorType.equals(SENSOR_TYPE_SMS) || sensorType.equals(SENSOR_TYPE_CONTACTS_CACHE_CHANGED)) {
                    // search for sms events, save start time
                    //PPApplication.logE("EventsHandler.handleEvents", "search for sms events");
                    for (Event _event : dataWrapper.eventList) {
                        if (_event.getStatus() != Event.ESTATUS_STOP) {
                            if (_event._eventPreferencesSMS._enabled) {
                                //PPApplication.logE("EventsHandler.handleEvents", "event._id=" + _event._id);
                                _event._eventPreferencesSMS.saveStartTime(dataWrapper, eventSMSPhoneNumber, eventSMSDate, eventSMSFromSIMSlot);
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
                if (sensorType.equals(SENSOR_TYPE_PHONE_CALL) || sensorType.equals(SENSOR_TYPE_CONTACTS_CACHE_CHANGED)) {
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

            Profile oldActivatedProfile = dataWrapper.getActivatedProfileFromDB(false, false);
            //Profile oldActivatedProfile = Profile.getProfileFromSharedPreferences(context, PPApplication.ACTIVATED_PROFILE_PREFS_NAME);
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
                        ListenableFuture<List<WorkInfo>> statuses = instance.getWorkInfosForUniqueWork("restartEventsWithDelayWork");
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

//            PPApplication.logE("[APP_START] EventsHandler.handleEvents", "continue (3)");

            List<EventTimeline> eventTimelineList = dataWrapper.getEventTimelineList(false);
            //int runningEventCount0 = eventTimelineList.size();

            sortEventsByStartOrderDesc(dataWrapper.eventList);
            if (isRestart) {
//                PPApplication.logE("[APP_START] EventsHandler.handleEvents", "continue (4)");

                /*if (PPApplication.logEnabled()) {
                    PPApplication.logE("$$$ EventsHandler.handleEvents", "restart events");
                    PPApplication.logE("[DEFPROF] EventsHandler.handleEvents", "restart events");
                }*/
//                PPApplication.logE("[FIFO_TEST] EventsHandler.handleEvents", "restart events");

                //reactivateProfile = true;

                // 1. pause events
                for (Event _event : dataWrapper.eventList) {
//                    if (PPApplication.logEnabled()) {
//                        PPApplication.logE("[FIFO_TEST] EventsHandler.handleEvents", "state PAUSE");
//                        PPApplication.logE("[FIFO_TEST] EventsHandler.handleEvents", "event._name=" + _event._name);
//                        PPApplication.logE("[FIFO_TEST] EventsHandler.handleEvents", "event.getStatus()=" + _event.getStatus());
//                    }

                    if (_event.getStatus() != Event.ESTATUS_STOP) {
                        // only pause events
                        // pause also paused events

                        /*if (PPApplication.logEnabled()) {
                            PPApplication.logE("[DEFPROF] EventsHandler.handleEvents", "state PAUSE");
                            PPApplication.logE("[DEFPROF] EventsHandler.handleEvents", "event._name=" + _event._name);
                            PPApplication.logE("[DEFPROF] EventsHandler.handleEvents", "event.getStatus()=" + _event.getStatus());
                        }*/

                        boolean running = _event.getStatus() == Event.ESTATUS_RUNNING;
                        doHandleEvent(_event, true, /*sensorType,*/ true, /*manualRestart,*/ false, false, /*reactivateProfile,*/ mergedProfile, dataWrapper);
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

//                        if (PPApplication.logEnabled()) {
//                            PPApplication.logE("[FIFO_TEST] ----- EventsHandler.handleEvents", "state PAUSE");
//                            PPApplication.logE("[FIFO_TEST] ----- EventsHandler.handleEvents", "event._name=" + _event._name);
//                            PPApplication.logE("[FIFO_TEST] ----- EventsHandler.handleEvents", "event.getStatus()=" + _event.getStatus());
//                            PPApplication.logE("[FIFO_TEST] ----- EventsHandler.handleEvents", "mergedProfilesCount=" + mergedProfilesCount);
//                            PPApplication.logE("[FIFO_TEST] ----- EventsHandler.handleEvents", "usedEventsCount=" + usedEventsCount);
//                        }
                    }
                }

//                PPApplication.logE("[FIFO_TEST] EventsHandler.handleEvents", "#### clear for pause - restart events");
                synchronized (PPApplication.profileActivationMutex) {
                    List<String> activateProfilesFIFO = new ArrayList<>();
                    dataWrapper.saveActivatedProfilesFIFO(activateProfilesFIFO);
                }


                //runningEventCountP = _etl.size();

                // 2. start events
                //dataWrapper.sortEventsByStartOrderAsc();
                Collections.reverse(dataWrapper.eventList);
                for (Event _event : dataWrapper.eventList) {
//                    if (PPApplication.logEnabled()) {
//                        PPApplication.logE("[FIFO_TEST] EventsHandler.handleEvents", "state RUNNING");
//                        PPApplication.logE("[FIFO_TEST] EventsHandler.handleEvents", "event.name=" + _event._name);
//                        PPApplication.logE("[FIFO_TEST] EventsHandler.handleEvents", "event.getStatus()=" + _event.getStatus());
//                    }

                    if (_event.getStatus() != Event.ESTATUS_STOP) {
                        // only start events

                        /*if (PPApplication.logEnabled()) {
                            PPApplication.logE("[DEFPROF] EventsHandler.handleEvents", "state RUNNING");
                            PPApplication.logE("[DEFPROF] EventsHandler.handleEvents", "event._name=" + _event._name);
                            PPApplication.logE("[DEFPROF] EventsHandler.handleEvents", "event.getStatus()=" + _event.getStatus());
                        }*/

                        // start all events
                        boolean paused = _event.getStatus() == Event.ESTATUS_PAUSE;
                        doHandleEvent(_event, false, /*sensorType,*/ true, /*manualRestart,*/ false, false, /*reactivateProfile,*/ mergedProfile, dataWrapper);
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

//                        if (PPApplication.logEnabled()) {
//                            PPApplication.logE("[FIFO_TEST] ----- EventsHandler.handleEvents", "state RUNNING");
//                            PPApplication.logE("[FIFO_TEST] ----- EventsHandler.handleEvents", "event._name=" + _event._name);
//                            PPApplication.logE("[FIFO_TEST] ----- EventsHandler.handleEvents", "event.getStatus()=" + _event.getStatus());
//                            PPApplication.logE("[FIFO_TEST] ----- EventsHandler.handleEvents", "paused=" + paused);
//                            PPApplication.logE("[FIFO_TEST] ----- EventsHandler.handleEvents", "running=" + running);
//                            PPApplication.logE("[FIFO_TEST] ----- EventsHandler.handleEvents", "mergedProfilesCount=" + mergedProfilesCount);
//                            PPApplication.logE("[FIFO_TEST] ----- EventsHandler.handleEvents", "usedEventsCount=" + usedEventsCount);
//                        }
                    }
                }
            } else {
//                PPApplication.logE("[APP_START] EventsHandler.handleEvents", "continue (5)");

                //PPApplication.logE("[TEST BATTERY]  EventsHandler.handleEvents", "NO restart events");
                /*if (PPApplication.logEnabled()) {
                    PPApplication.logE("$$$ EventsHandler.handleEvents", "NO restart events");
                    PPApplication.logE("[DEFPROF] EventsHandler.handleEvents", "NO restart events");
                }*/

                //1. pause events
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
                        doHandleEvent(_event, true, /*sensorType,*/ false, /*false,*/ forDelayStartAlarm, forDelayEndAlarm, /*reactivateProfile,*/ mergedProfile, dataWrapper);
                        boolean paused = _event.getStatus() == Event.ESTATUS_PAUSE;

                        if (running && paused) {
                            if (startProfileMerged)
                                mergedProfilesCount++;
                            if (endProfileMerged)
                                mergedProfilesCount++;
                            if (startProfileMerged || endProfileMerged)
                                usedEventsCount++;

                            //if (_event._undoCalled)
                            //    undoCalled = true;

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

//                        PPApplication.logE("[FIFO_TEST] ----- EventsHandler.handleEvents", "state PAUSE");
//                        PPApplication.logE("[FIFO_TEST] ----- EventsHandler.handleEvents", "event._name=" + _event._name);
//                        PPApplication.logE("[FIFO_TEST] ----- EventsHandler.handleEvents", "event.getStatus()=" + _event.getStatus());
//                        PPApplication.logE("[FIFO_TEST] ----- EventsHandler.handleEvents", "mergedProfilesCount=" + mergedProfilesCount);
//                        PPApplication.logE("[FIFO_TEST] ----- EventsHandler.handleEvents", "usedEventsCount=" + usedEventsCount);
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
                        doHandleEvent(_event, false, /*sensorType,*/ false, /*false,*/ forDelayStartAlarm, forDelayEndAlarm, /*true*//*reactivateProfile,*/ mergedProfile, dataWrapper);
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

//                        PPApplication.logE("[FIFO_TEST] ----- EventsHandler.handleEvents", "state RUNNING");
//                        PPApplication.logE("[FIFO_TEST] ----- EventsHandler.handleEvents", "event._name=" + _event._name);
//                        PPApplication.logE("[FIFO_TEST] ----- EventsHandler.handleEvents", "event.getStatus()=" + _event.getStatus());
//                        PPApplication.logE("[FIFO_TEST] ----- EventsHandler.handleEvents", "mergedProfilesCount=" + mergedProfilesCount);
//                        PPApplication.logE("[FIFO_TEST] ----- EventsHandler.handleEvents", "usedEventsCount=" + usedEventsCount);
                    }
                }
            }

            PPApplication.lockRefresh = false;

//            PPApplication.logE("[APP_START] EventsHandler.handleEvents (02)", "PPApplication.applicationFullyStarted="+PPApplication.applicationFullyStarted);

//            if (isRestart) {
//                if (mergedProfile._id == 0)
//                    PPApplication.logE("[FIFO_TEST] EventsHandler.handleEvents", "no profile for activation");
//                else
//                    PPApplication.logE("[FIFO_TEST] EventsHandler.handleEvents", "profileName=" + mergedProfile._name);
//            }

            //if ((!restartAtEndOfEvent) || isRestart) {
            //    // No any paused events has "Restart events" at end of event

            //////////////////
            //// when no events are running or manual activation,
            //// activate background profile when no profile is activated

            // get running events count
            int runningEventCountE = eventTimelineList.size();

            // activated profile may be changed, when event has enabled manual profile activation
            Profile semiOldActivatedProfile = dataWrapper.getActivatedProfileFromDB(false, false);
//            if (isRestart) {
//                if (activatedProfile != null)
//                    PPApplication.logE("[FIFO_TEST] EventsHandler.handleEvents", "activatedProfile._name=" + activatedProfile._name);
//                else
//                    PPApplication.logE("[FIFO_TEST] EventsHandler.handleEvents", "not profile activated");
//            }
            long defaultProfileId = Profile.PROFILE_NO_ACTIVATE;
            boolean notifyDefaultProfile = false;
            boolean isAnyEventEnabled =  DatabaseHandler.getInstance(context.getApplicationContext()).isAnyEventEnabled();

            //boolean fullyStarted = false;
            //if (ppService != null)
            //    fullyStarted = ppService.getApplicationFullyStarted();

//            PPApplication.logE("[APP_START] EventsHandler.handleEvents", "continue (6)");

            if (!DataWrapper.getIsManualProfileActivation(false, context)) {
                // no manual profile activation
//                PPApplication.logE("[APP_START] EventsHandler.handleEvents", "continue (7)");

                //                if (PPApplication.logEnabled()) {
//                    if (isRestart) {
//                        PPApplication.logE("[FIFO_TEST] EventsHandler.handleEvents", "active profile is NOT activated manually");
//                        PPApplication.logE("[FIFO_TEST] EventsHandler.handleEvents", "runningEventCountE=" + runningEventCountE);
//                    }
//                }

//                PPApplication.logE("[APP_START] EventsHandler.handleEvents", "runningEventCountE="+runningEventCountE);

                if (runningEventCountE == 0) {
                    // activate default profile

//                    if (isRestart)
//                        PPApplication.logE("[FIFO_TEST] EventsHandler.handleEvents", "no events running");
                    // no events running

//                    PPApplication.logE("[APP_START] EventsHandler.handleEvents (1)", "PPApplication.applicationFullyStarted="+PPApplication.applicationFullyStarted);

                    // THIS MUST BE PURE DEFAULT PROFILE, BECAUSE IT IS TESTED
                    defaultProfileId = ApplicationPreferences.applicationDefaultProfile;

//                    PPApplication.logE("[APP_START] EventsHandler.handleEvents", "defaultProfileId="+defaultProfileId);

                    //if (!fullyStarted)
                    //    defaultProfileId = Profile.PROFILE_NO_ACTIVATE;
                    if ((defaultProfileId != Profile.PROFILE_NO_ACTIVATE) && isAnyEventEnabled) {
//                        if (isRestart)
//                            PPApplication.logE("[FIFO_TEST] EventsHandler.handleEvents", "default profile is set");

//                        PPApplication.logE("[APP_START] EventsHandler.handleEvents (1)", "PPApplication.applicationFullyStarted="+PPApplication.applicationFullyStarted);
                        defaultProfileId = ApplicationPreferences.getApplicationDefaultProfileOnBoot();
//                        PPApplication.logE("[APP_START] EventsHandler.handleEvents", "getApplicationDefaultProfileOnBoot()="+defaultProfileId);

                        long semiOldActivatedProfileId = 0;
                        if (semiOldActivatedProfile != null)
                            semiOldActivatedProfileId = semiOldActivatedProfile._id;

//                        PPApplication.logE("[APP_START] EventsHandler.handleEvents", "semiOldActivatedProfileId="+semiOldActivatedProfileId);

                        boolean defaultProfileActivated = false;
                        if ((semiOldActivatedProfileId == 0) ||
                                isRestart ||
                                (semiOldActivatedProfileId != defaultProfileId)) {
                            mergedProfile.mergeProfiles(defaultProfileId, dataWrapper/*, false*/);
                            notifyDefaultProfile = true;

                            defaultProfileActivated = true;
                            mergedProfilesCount++;
//                            if (isRestart)
//                                PPApplication.logE("[FIFO_TEST] EventsHandler.handleEvents", "activated default profile");

//                            PPApplication.logE("[FIFO_TEST] EventsHandler.handleEvents", "#### add default profile - profileId=" + defaultProfileId);
//                            PPApplication.logE("[APP_START] EventsHandler.handleEvents", "#### add default profile - profileId=" + defaultProfileId);
                            dataWrapper.addProfileToFIFO(defaultProfileId, 0);
                        }

//                        PPApplication.logE("[BLOCK_ACTIONS] EventsHanlder.handleEvents", "sensorType="+sensorType);
//                        PPApplication.logE("[BLOCK_ACTIONS] EventsHanlder.handleEvents", "defaultProfileId="+defaultProfileId);
//                        PPApplication.logE("[BLOCK_ACTIONS] EventsHanlder.handleEvents", "semiOldActivatedProfileId="+semiOldActivatedProfileId);
//                        PPApplication.logE("[BLOCK_ACTIONS] EventsHanlder.handleEvents", "isRestart="+isRestart);
//                        PPApplication.logE("[BLOCK_ACTIONS] EventsHanlder.handleEvents", "manualRestart="+manualRestart);
//                        PPApplication.logE("[BLOCK_ACTIONS] EventsHanlder.handleEvents", "mergedProfile._id="+mergedProfile._id);
                        if (((semiOldActivatedProfileId == defaultProfileId) &&
                                ((mergedProfilesCount > 0) || defaultProfileActivated)) ||
                            (isRestart && (!manualRestart))) {
                            // block interactive parameters when
                            // - activated profile is default profile
                            // - it is not manual restart of events
//                            PPApplication.logE("[BLOCK_ACTIONS] EventsHanlder.handleEvents", "true");
                            PPApplication.setBlockProfileEventActions(true);
                        }

                        /*if (!isRestart) {
                            PPApplication.logE("[APP_START] EventsHandler.handleEvents", "setApplicationFullyStarted (02)");
                            PPApplication.setApplicationFullyStarted(context);
                        }*/

                    } else {
                        /*PPApplication.logE("[APP_START] EventsHandler.handleEvents", "setApplicationFullyStarted (03)");
                        PPApplication.setApplicationFullyStarted(context);*/
                        if (PPApplication.prefLastActivatedProfile != 0) {
//                            PPApplication.logE("[FIFO_TEST] EventsHandler.handleEvents", "#### add PPApplication.prefLastActivatedProfile - profileId=" + PPApplication.prefLastActivatedProfile);
                            dataWrapper.addProfileToFIFO(PPApplication.prefLastActivatedProfile, 0);
                        }
                    }
                }
                /*else {
                    PPApplication.logE("[APP_START] EventsHandler.handleEvents", "setApplicationFullyStarted (04)");
                    PPApplication.setApplicationFullyStarted(context);
                }*/
            } else {
                // manual profile activation

 //                if (isRestart)
//                    PPApplication.logE("[FIFO_TEST] EventsHandler.handleEvents", "active profile is activated manually");

                /*
                PPApplication.logE("[APP_START] EventsHandler.handleEvents", "setApplicationFullyStarted (05)");
                PPApplication.setApplicationFullyStarted(context);
                */

                boolean defaultProfileActivated = false;

                long semiOldActivatedProfileId = 0;
                if (semiOldActivatedProfile != null)
                    semiOldActivatedProfileId = semiOldActivatedProfile._id;

                if (semiOldActivatedProfileId > 0) {
                    // any profile activated, set back semi-old, this uses profile activated by events

                    //noinspection ConstantConditions
                    defaultProfileId = Profile.PROFILE_NO_ACTIVATE;
                    mergedProfile.mergeProfiles(semiOldActivatedProfileId, dataWrapper/*, false*/);
                    //mergedProfilesCount++;
//                    if (isRestart)
//                        PPApplication.logE("[FIFO_TEST] EventsHandler.handleEvents", "activated old profile");

//                    PPApplication.logE("[FIFO_TEST] EventsHandler.handleEvents", "#### add semi-old activated profile - profileId=" + semiOldActivatedProfileId);
                    dataWrapper.addProfileToFIFO(semiOldActivatedProfileId, 0);
                }
                else {
                    // not any profile activated

//                    PPApplication.logE("EventsHandler.handleEvents (2)", "PPApplication.applicationFullyStarted="+PPApplication.applicationFullyStarted);
//                    PPApplication.logE("[APP_START] EventsHandler.handleEvents (2)", "PPApplication.applicationFullyStarted="+PPApplication.applicationFullyStarted);
                    defaultProfileId = ApplicationPreferences.getApplicationDefaultProfileOnBoot();
                    //defaultProfileId = ApplicationPreferences.applicationDefaultProfile;

                    //if (!fullyStarted)
                    //    defaultProfileId = Profile.PROFILE_NO_ACTIVATE;
                    if ((defaultProfileId != Profile.PROFILE_NO_ACTIVATE) && isAnyEventEnabled) {
                        // if not any profile activated, activate default profile
                        notifyDefaultProfile = true;
                        mergedProfile.mergeProfiles(defaultProfileId, dataWrapper/*, false*/);

                        defaultProfileActivated = true;
                        mergedProfilesCount++;
//                        if (isRestart)
//                            PPApplication.logE("[FIFO_TEST] EventsHandler.handleEvents", "activated default profile");

//                        PPApplication.logE("[FIFO_TEST] EventsHandler.handleEvents", "#### add default profile - profileId=" + PPApplication.prefLastActivatedProfile);
                        dataWrapper.addProfileToFIFO(defaultProfileId, 0);
                    } else {
                        if (PPApplication.prefLastActivatedProfile != 0) {
//                            PPApplication.logE("[FIFO_TEST] EventsHandler.handleEvents", "#### add PPApplication.prefLastActivatedProfile - profileId=" + PPApplication.prefLastActivatedProfile);
                            dataWrapper.addProfileToFIFO(PPApplication.prefLastActivatedProfile, 0);
                        }
                    }

//                    PPApplication.logE("[BLOCK_ACTIONS] EventsHanlder.handleEvents", "sensorType="+sensorType);
//                    PPApplication.logE("[BLOCK_ACTIONS] EventsHanlder.handleEvents", "defaultProfileId="+defaultProfileId);
//                    PPApplication.logE("[BLOCK_ACTIONS] EventsHanlder.handleEvents", "semiOldActivatedProfileId="+semiOldActivatedProfileId);
//                    PPApplication.logE("[BLOCK_ACTIONS] EventsHanlder.handleEvents", "isRestart="+isRestart);
//                    PPApplication.logE("[BLOCK_ACTIONS] EventsHanlder.handleEvents", "manualRestart="+manualRestart);
//                    PPApplication.logE("[BLOCK_ACTIONS] EventsHanlder.handleEvents", "mergedProfile._id="+mergedProfile._id);

                    if (isAnyEventEnabled) {
                        if (((semiOldActivatedProfileId == defaultProfileId) &&
                                ((mergedProfilesCount > 0) || defaultProfileActivated)) ||
                                (isRestart && (!manualRestart))) {
                            // block interactive parameters when
                            // - activated profile is default profile
                            // - it is not manual restart of events
//                        PPApplication.logE("[BLOCK_ACTIONS] EventsHanlder.handleEvents", "true");
                            PPApplication.setBlockProfileEventActions(true);
                        }
                    }
                }
            }
            ////////////////

            //Event notifyEventStart = null;
            String defaultProfileNotificationSound = "";
            boolean defaultProfileNotificationVibrate = false;

            if (/*(!isRestart) &&*/ (defaultProfileId != Profile.PROFILE_NO_ACTIVATE) && isAnyEventEnabled && notifyDefaultProfile) {
                // only when activated is background profile, play event notification sound

                defaultProfileNotificationSound = ApplicationPreferences.applicationDefaultProfileNotificationSound;
                defaultProfileNotificationVibrate = ApplicationPreferences.applicationDefaultProfileNotificationVibrate;
            }

//            if (PPApplication.logEnabled()) {
//                if (isRestart) {
//                    PPApplication.logE("[FIFO_TEST] EventsHandler.handleEvents", "mergedProfilesCount=" + mergedProfilesCount);
//                    PPApplication.logE("[FIFO_TEST] EventsHandler.handleEvents", "usedEventsCount=" + usedEventsCount);
//
//                    PPApplication.logE("[FIFO_TEST] EventsHandler.handleEvents", "mergedProfile=" + mergedProfile);
//                    PPApplication.logE("[FIFO_TEST] EventsHandler.handleEvents", "mergedProfile._id=" + mergedProfile._id);
//                }
//            }

            boolean doSleep = false;

//            if (isRestart)
//                PPApplication.logE("[FIFO_TEST]  EventsHandler.handleEvents", "mergedProfile._name="+mergedProfile._name);

            if (mergedProfile._id != 0) {
                // activate merged profile
//                if (PPApplication.logEnabled()) {
//                    if (isRestart) {
//                        PPApplication.logE("[FIFO_TEST]  EventsHandler.handleEvents", "#### oldActivatedProfile-profileName=" + oldActivatedProfile._name);
//                        //PPApplication.logE("$$$ EventsHandler.handleEvents", "#### oldActivatedProfile-profileId=" + oldActivatedProfile._id);
//
//                        PPApplication.logE("[FIFO_TEST]  EventsHandler.handleEvents", "#### mergedProfile-profileName=" + mergedProfile._name);
//                        //PPApplication.logE("$$$ EventsHandler.handleEvents", "#### mergedProfile-profileId=" + mergedProfile._id);
//                        //PPApplication.logE("$$$ EventsHandler.handleEvents", "#### mergedProfile-_volumeRingerMode=" + mergedProfile._volumeRingerMode);
//                        //PPApplication.logE("$$$ EventsHandler.handleEvents", "#### mergedProfile-_volumeZenMode=" + mergedProfile._volumeZenMode);
//                        //PPApplication.logE("$$$ EventsHandler.handleEvents", "#### mergedProfile-_volumeRingtone=" + mergedProfile._volumeRingtone);
//                        //PPApplication.logE("$$$ EventsHandler.handleEvents", "#### mergedProfile-_volumeNotification=" + mergedProfile._volumeNotification);
//                    }
//                }
                DatabaseHandler.getInstance(context.getApplicationContext()).saveMergedProfile(mergedProfile);

                // check if profile has changed
                if (!mergedProfile.compareProfile(oldActivatedProfile))
                    profileChanged = true;

//                if (isRestart)
//                    PPApplication.logE("[FIFO_TEST]  EventsHandler.handleEvents", "#### profileChanged=" + profileChanged);
//                PPApplication.logE("[FIFO_TEST] EventsHandler.handleEvents", "#### isRestart=" + isRestart);
//                PPApplication.logE("[FIFO_TEST] EventsHandler.handleEvents", "#### profileChanged=" + profileChanged);

                if (profileChanged || (usedEventsCount > 0) || isRestart /*sensorType.equals(SENSOR_TYPE_MANUAL_RESTART_EVENTS)*/) {

                    // log only when merged profile is not the same as last activated or for restart events
                    PPApplication.addActivityLog(context, PPApplication.ALTYPE_MERGED_PROFILE_ACTIVATION, null,
                            DataWrapper.getProfileNameWithManualIndicatorAsString(mergedProfile, true, "", false, false, false, dataWrapper),
                            mergedProfile._icon, 0, mergedProfilesCount + " [" + usedEventsCount + "]");

//                    if (isRestart)
//                        PPApplication.logE("[FIFO_TEST] EventsHandler.handleEvents", "called is DataWrapper.activateProfileFromEvent");
                    dataWrapper.activateProfileFromEvent(0, mergedProfile._id, false, true, isRestart);
                    // wait for profile activation
                    doSleep = true;
                }
            } /*else {
                if (!DataWrapper.getIsManualProfileActivation(false, context)) {
                    // not manual profile activation
                    //if (isRestart) {
                        PPApplication.logE("[FIFO_TEST]  EventsHandler.handleEvents", "(2)");
                        PPApplication.logE("[FIFO_TEST]  EventsHandler.handleEvents", "PPApplication.prefLastActivatedProfile=" + PPApplication.prefLastActivatedProfile);
                        PPApplication.logE("[FIFO_TEST]  EventsHandler.handleEvents", "runningEventCountE=" + runningEventCountE);
                    //}

                    if (runningEventCountE == 0) {
                        // not any event is running
                        if (PPApplication.prefLastActivatedProfile != 0) {
                            // last activated profile is not 0
                            PPApplication.setLastActivatedProfile(context, 0);
                            // not needed, update gui is at end
                            //PPApplication.logE("###### PPApplication.updateGUI", "from=EventsHandler.handleEvents (3)");
                            //PPApplication.updateGUI(false); //context, true, true);
                        }
                    }
                }
            }*/

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
//                        if (isRestart)
//                            PPApplication.logE("[FIFO_TEST] EventsHandler.handleEvents", "default profile notified");
                        notified = true;
                    }
                }
            }

            if (doSleep || notified) {
                PPApplication.sleep(500);
            }
            //}

            //restartAtEndOfEvent = false;

            doEndHandler(dataWrapper, mergedProfile);

//            PPApplication.logE("[APP_START] EventsHandler.handleEvents", "setApplicationFullyStarted (XXX)");
            PPApplication.setApplicationFullyStarted(context);

            // refresh all GUI - must be for restart scanners
            //if (PPApplication.isScreenOn) {
            if (profileChanged || (usedEventsCount > 0) || isRestart /*sensorType.equals(SENSOR_TYPE_MANUAL_RESTART_EVENTS)*/) {
                PPApplication.updateGUI(1, context/*, true, true*/);

//                PPApplication.logE("[FIFO_TEST] EventsHandler.handleEvents", "#### in fifo is:");
                synchronized (PPApplication.profileActivationMutex) {
                    dataWrapper.getActivatedProfilesFIFO();
                }
            }
            else {
                // refresh only Editor
                Intent refreshIntent = new Intent(PPApplication.PACKAGE_NAME + ".RefreshEditorGUIBroadcastReceiver");
                refreshIntent.putExtra(RefreshActivitiesBroadcastReceiver.EXTRA_REFRESH_ICONS, false);
                //refreshIntent.putExtra(PPApplication.EXTRA_PROFILE_ID, profileId);
                //refreshIntent.putExtra(PPApplication.EXTRA_EVENT_ID, eventId);
                LocalBroadcastManager.getInstance(context).sendBroadcast(refreshIntent);
            }


            //dataWrapper.invalidateDataWrapper();

//            if (isRestart)
//                PPApplication.logE("[FIFO_TEST] EventsHandler.handleEvents", "-- end --------------------------------");

//                PPApplication.logE("[IN_EVENTS_HANDLER] EventsHandler.handleEvents", "-- end --------------------------------");

        }
    }

    private boolean alwaysEnabledSensors (String sensorType) {
        switch (sensorType) {
            case SENSOR_TYPE_SCREEN:
                // call doHandleEvents for all screen on/off changes
                //eventType = DatabaseHandler.ETYPE_SCREEN;
                //sensorEnabled = _event._eventPreferencesScreen._enabled;
            case SENSOR_TYPE_PERIODIC_EVENTS_HANDLER:
            case SENSOR_TYPE_RESTART_EVENTS:
            case SENSOR_TYPE_MANUAL_RESTART_EVENTS:
            case SENSOR_TYPE_EVENT_DELAY_START:
            case SENSOR_TYPE_EVENT_DELAY_END:
            case SENSOR_TYPE_DEVICE_IDLE_MODE:
                return true;
        }
        return false;
    }

    private int getEventTypeForSensor(String sensorType) {
        switch (sensorType) {
            case SENSOR_TYPE_BATTERY:
            case SENSOR_TYPE_POWER_SAVE_MODE:
                return DatabaseHandler.ETYPE_BATTERY;
            case SENSOR_TYPE_BATTERY_WITH_LEVEL:
                return DatabaseHandler.ETYPE_BATTERY_WITH_LEVEL;
            case SENSOR_TYPE_BLUETOOTH_CONNECTION:
                return DatabaseHandler.ETYPE_BLUETOOTH_CONNECTED;
            case SENSOR_TYPE_BLUETOOTH_SCANNER:
            case SENSOR_TYPE_BLUETOOTH_STATE:
                return DatabaseHandler.ETYPE_BLUETOOTH_NEARBY;
            case SENSOR_TYPE_CALENDAR_PROVIDER_CHANGED:
            case SENSOR_TYPE_CALENDAR:
            case SENSOR_TYPE_SEARCH_CALENDAR_EVENTS:
            case SENSOR_TYPE_CALENDAR_EVENT_EXISTS_CHECK:
                return DatabaseHandler.ETYPE_CALENDAR;
            case SENSOR_TYPE_DOCK_CONNECTION:
            case SENSOR_TYPE_HEADSET_CONNECTION:
                return DatabaseHandler.ETYPE_ACCESSORY;
            case SENSOR_TYPE_TIME:
                return DatabaseHandler.ETYPE_TIME;
            case SENSOR_TYPE_APPLICATION:
                return DatabaseHandler.ETYPE_APPLICATION;
            case SENSOR_TYPE_NOTIFICATION:
                return DatabaseHandler.ETYPE_NOTIFICATION;
            /*case SENSOR_TYPE_NOTIFICATION_EVENT_END:
                return DatabaseHandler.ETYPE_NOTIFICATION;*/
            case SENSOR_TYPE_PHONE_CALL:
            case SENSOR_TYPE_PHONE_CALL_EVENT_END:
                return DatabaseHandler.ETYPE_CALL;
            case SENSOR_TYPE_SMS:
            case SENSOR_TYPE_SMS_EVENT_END:
                return DatabaseHandler.ETYPE_SMS;
            case SENSOR_TYPE_WIFI_CONNECTION:
                return DatabaseHandler.ETYPE_WIFI_CONNECTED;
            case SENSOR_TYPE_WIFI_SCANNER:
            case SENSOR_TYPE_WIFI_STATE:
                return DatabaseHandler.ETYPE_WIFI_NEARBY;
            case SENSOR_TYPE_LOCATION_SCANNER:
            case SENSOR_TYPE_LOCATION_MODE:
                return DatabaseHandler.ETYPE_LOCATION;
            case SENSOR_TYPE_DEVICE_ORIENTATION:
                return DatabaseHandler.ETYPE_ORIENTATION;
            case SENSOR_TYPE_MOBILE_CELLS:
                return DatabaseHandler.ETYPE_MOBILE_CELLS;
            case SENSOR_TYPE_NFC_TAG:
            case SENSOR_TYPE_NFC_EVENT_END:
                return DatabaseHandler.ETYPE_NFC;
            case SENSOR_TYPE_RADIO_SWITCH:
                return DatabaseHandler.ETYPE_RADIO_SWITCH;
            case SENSOR_TYPE_ALARM_CLOCK:
            case SENSOR_TYPE_ALARM_CLOCK_EVENT_END:
                return DatabaseHandler.ETYPE_ALARM_CLOCK;
            case SENSOR_TYPE_DEVICE_BOOT:
            case SENSOR_TYPE_DEVICE_BOOT_EVENT_END:
                return DatabaseHandler.ETYPE_DEVICE_BOOT;
            default:
                return DatabaseHandler.ETYPE_ALL;
        }
    }

    /*
    private boolean eventsExists(String sensorType, DataWrapper dataWrapper) {

        boolean sensorEnabled;
        for (Event _event : dataWrapper.eventList) {
            //boolean eventEnabled;
            //if (onlyRunning)
            //    eventEnabled = _event.getStatus() == Event.ESTATUS_RUNNING;
            //else
            //    eventEnabled = _event.getStatus() != Event.ESTATUS_STOP;
            if (_event.getStatus() != Event.ESTATUS_STOP) {
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
                    case SENSOR_TYPE_CALENDAR_EVENT_EXISTS_CHECK:
                        //eventType = DatabaseHandler.ETYPE_CALENDAR;
                        sensorEnabled = _event._eventPreferencesCalendar._enabled;
                        break;
                    case SENSOR_TYPE_DOCK_CONNECTION:
                    case SENSOR_TYPE_HEADSET_CONNECTION:
                        //eventType = DatabaseHandler.ETYPE_ACCESSORY;
                        sensorEnabled = _event._eventPreferencesAccessories._enabled;
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
                    //case SENSOR_TYPE_NOTIFICATION_EVENT_END:
                    //    eventType = DatabaseHandler.ETYPE_NOTIFICATION;
                    //    break;
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
                    case SENSOR_TYPE_LOCATION_SCANNER:
                    case SENSOR_TYPE_LOCATION_MODE:
                        //eventType = DatabaseHandler.ETYPE_LOCATION;
                        sensorEnabled = _event._eventPreferencesLocation._enabled;
                        break;
                    case SENSOR_TYPE_DEVICE_ORIENTATION:
                        //eventType = DatabaseHandler.ETYPE_ORIENTATION;
                        sensorEnabled = _event._eventPreferencesOrientation._enabled;
                        break;
                    case SENSOR_TYPE_MOBILE_CELLS:
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
                    default:
                        sensorEnabled = true;
                        break;
                }

                if (!sensorEnabled)
                    sensorEnabled = alwaysEnabledSensors(sensorType);

                if (sensorEnabled)
                    return true;
            }
        }
        return alwaysEnabledSensors(sensorType);
    }
    */

    private void doEndHandler(DataWrapper dataWrapper, Profile mergedProfile) {
//        PPApplication.logE("EventsHandler.doEndHandler","sensorType="+sensorType);
        //PPApplication.logE("EventsHandler.doEndHandler","callEventType="+callEventType);

        if (sensorType.equals(SENSOR_TYPE_PHONE_CALL) && (dataWrapper != null)) {
            TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

//            PPApplication.logE("EventsHandler.doEndHandler", "SENSOR_TYPE_PHONE_CALL - running event exists");
            // doEndHandler is called even if no event exists, but ringing call simulation is only for running event with call sensor
            //if (android.os.Build.VERSION.SDK_INT >= 21) {
            boolean inRinging = false;
            if (telephony != null) {
                /*int callState = TelephonyManager.CALL_STATE_IDLE; //telephony.getCallState();
                int simCount = telephony.getPhoneCount();
                PPApplication.logE("EventsHandler.doEndHandler", "simCount=" + simCount);
                if (simCount > 1) {
                    if (PPApplication.phoneCallsListenerSIM1 != null) {
                        callState = PPApplication.phoneCallsListenerSIM1.lastState;
                        PPApplication.logE("EventsHandler.doEndHandler", "callState (1)=" + callState);
                    }
                    if (callState != TelephonyManager.CALL_STATE_RINGING) {
                        if (PPApplication.phoneCallsListenerSIM2 != null) {
                            callState = PPApplication.phoneCallsListenerSIM2.lastState;
                            PPApplication.logE("EventsHandler.doEndHandler", "callState (2)=" + callState);
                        }
                    }
                } else {
                    if (PPApplication.phoneCallsListenerDefaul != null)
                        callState = PPApplication.phoneCallsListenerDefaul.lastState;
                }*/
                int callState = PPApplication.getCallState(context);
//                PPApplication.logE("EventsHandler.doEndHandler", "callState="+callState);
                //if (doUnlink) {
                //if (linkUnlink == PhoneCallsListener.LINKMODE_UNLINK) {
                inRinging = (callState == TelephonyManager.CALL_STATE_RINGING);
            }
//            PPApplication.logE("EventsHandler.doEndHandler", "inRinging="+inRinging);
            if (inRinging) {
                // start PhoneProfilesService for ringing call simulation
//                PPApplication.logE("EventsHandler.doEndHandler", "start simulating ringing call");
                try {
                    boolean simulateRingingCall = false;
                    String phoneNumber = ApplicationPreferences.prefEventCallPhoneNumber;
                    for (Event _event : dataWrapper.eventList) {
                        if (_event._eventPreferencesCall._enabled && _event.getStatus() == Event.ESTATUS_RUNNING) {
//                            PPApplication.logE("EventsHandler.doEndHandler", "event._id=" + _event._id);
                            if (_event._eventPreferencesCall.isPhoneNumberConfigured(phoneNumber/*, dataWrapper*/)) {
                                simulateRingingCall = true;
                                break;
                            }
                        }
                    }
//                    PPApplication.logE("EventsHandler.doEndHandler", "simulateRingingCall=" + simulateRingingCall);
                    int simSlot = ApplicationPreferences.prefEventCallFromSIMSlot;
                    if (simulateRingingCall) {
                        Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
                        commandIntent.putExtra(PhoneProfilesService.EXTRA_SIMULATE_RINGING_CALL, true);
                        // add saved ringer mode, zen mode, ringtone before handle events as parameters
                        // ringing call simulator compare this with new (actual values), changed by currently activated profile

//                        PPApplication.logE("EventsHandler.doEndHandler", "EXTRA_OLD_RINGER_MODE="+ oldRingerMode);
                        commandIntent.putExtra(PhoneProfilesService.EXTRA_OLD_RINGER_MODE, oldRingerMode);
                        //commandIntent.putExtra(PhoneProfilesService.EXTRA_OLD_SYSTEM_RINGER_MODE, oldSystemRingerMode);
//                        PPApplication.logE("EventsHandler.doEndHandler", "EXTRA_OLD_ZEN_MODE="+ oldZenMode);
                        commandIntent.putExtra(PhoneProfilesService.EXTRA_OLD_ZEN_MODE, oldZenMode);
//                        PPApplication.logE("EventsHandler.doEndHandler", "EXTRA_OLD_RINGTONE="+ oldRingtone);
                        commandIntent.putExtra(PhoneProfilesService.EXTRA_OLD_RINGTONE, oldRingtone);
//                        PPApplication.logE("EventsHandler.doEndHandler", "EXTRA_OLD_RINGTONE_SIM1="+ oldRingtoneSIM1);
                        commandIntent.putExtra(PhoneProfilesService.EXTRA_OLD_RINGTONE_SIM1, oldRingtoneSIM1);
//                        PPApplication.logE("EventsHandler.doEndHandler", "EXTRA_OLD_RINGTONE_SIM2="+ oldRingtoneSIM2);
                        commandIntent.putExtra(PhoneProfilesService.EXTRA_OLD_RINGTONE_SIM2, oldRingtoneSIM2);
                        //commandIntent.putExtra(PhoneProfilesService.EXTRA_OLD_SYSTEM_RINGER_VOLUME, oldSystemRingerVolume);

//                        PPApplication.logE("EventsHandler.doEndHandler", "EXTRA_NEW_RINGER_MODE="+ mergedProfile._volumeRingerMode);
                        commandIntent.putExtra(PhoneProfilesService.EXTRA_NEW_RINGER_MODE, mergedProfile._volumeRingerMode);
//                        PPApplication.logE("EventsHandler.doEndHandler", "EXTRA_NEW_ZEN_MODE="+ mergedProfile._volumeZenMode);
                        commandIntent.putExtra(PhoneProfilesService.EXTRA_NEW_ZEN_MODE, mergedProfile._volumeZenMode);
//                        PPApplication.logE("EventsHandler.doEndHandler", "EXTRA_NEW_RINGER_VOLUME="+ mergedProfile._volumeRingtone);
                        commandIntent.putExtra(PhoneProfilesService.EXTRA_NEW_RINGER_VOLUME, mergedProfile._volumeRingtone);
//                        PPApplication.logE("EventsHandler.doEndHandler", "EXTRA_NEW_RINTONE_CHANGE="+ mergedProfile._soundRingtoneChange);
                        commandIntent.putExtra(PhoneProfilesService.EXTRA_NEW_RINTONE_CHANGE, mergedProfile._soundRingtoneChange);
//                        PPApplication.logE("EventsHandler.doEndHandler", "EXTRA_NEW_RINGTONE="+ mergedProfile._soundRingtone);
                        commandIntent.putExtra(PhoneProfilesService.EXTRA_NEW_RINGTONE, mergedProfile._soundRingtone);
//                        PPApplication.logE("EventsHandler.doEndHandler", "EXTRA_NEW_RINTONE_CHANGE_SIM1="+ mergedProfile._soundRingtoneChangeSIM1);
                        commandIntent.putExtra(PhoneProfilesService.EXTRA_NEW_RINTONE_CHANGE_SIM1, mergedProfile._soundRingtoneChangeSIM1);
//                        PPApplication.logE("EventsHandler.doEndHandler", "EXTRA_NEW_RINGTONE_SIM1="+ mergedProfile._soundRingtoneSIM1);
                        commandIntent.putExtra(PhoneProfilesService.EXTRA_NEW_RINGTONE_SIM1, mergedProfile._soundRingtoneSIM1);
//                        PPApplication.logE("EventsHandler.doEndHandler", "EXTRA_NEW_RINTONE_CHANGE_SIM2="+ mergedProfile._soundRingtoneChangeSIM2);
                        commandIntent.putExtra(PhoneProfilesService.EXTRA_NEW_RINTONE_CHANGE_SIM2, mergedProfile._soundRingtoneChangeSIM2);
//                        PPApplication.logE("EventsHandler.doEndHandler", "EXTRA_NEW_RINGTONE_SIM2="+ mergedProfile._soundRingtoneSIM2);
                        commandIntent.putExtra(PhoneProfilesService.EXTRA_NEW_RINGTONE_SIM2, mergedProfile._soundRingtoneSIM2);

//                        PPApplication.logE("EventsHandler.doEndHandler", "EXTRA_CALL_FROM_SIM_SLOT="+ simSlot);
                        commandIntent.putExtra(PhoneProfilesService.EXTRA_CALL_FROM_SIM_SLOT, simSlot);
                        PPApplication.runCommand(context, commandIntent);
                    }
                } catch (Exception e) {
                    PPApplication.recordException(e);
                }
            }

            boolean inCall = false;
            if (telephony != null) {
                /*int callState = TelephonyManager.CALL_STATE_IDLE; //telephony.getCallState();
                int simCount = telephony.getPhoneCount();
                if (simCount > 1) {
                    if (PPApplication.phoneCallsListenerSIM1 != null)
                        callState = PPApplication.phoneCallsListenerSIM1.lastState;
                    if (callState != TelephonyManager.CALL_STATE_RINGING) {
                        if (PPApplication.phoneCallsListenerSIM2 != null)
                            callState = PPApplication.phoneCallsListenerSIM2.lastState;
                    }
                }
                else {
                    if (PPApplication.phoneCallsListenerDefaul != null)
                        callState = PPApplication.phoneCallsListenerDefaul.lastState;
                }*/
                int callState = PPApplication.getCallState(context);

                //if (doUnlink) {
                //if (linkUnlink == PhoneCallsListener.LINKMODE_UNLINK) {
                inCall = (callState == TelephonyManager.CALL_STATE_RINGING) || (callState == TelephonyManager.CALL_STATE_OFFHOOK);
            }
            if (!inCall)
                setEventCallParameters(EventPreferencesCall.PHONE_CALL_EVENT_UNDEFINED, "", 0, 0);
        }
        else
        if (sensorType.equals(SENSOR_TYPE_PHONE_CALL_EVENT_END)) {
            setEventCallParameters(EventPreferencesCall.PHONE_CALL_EVENT_UNDEFINED, "", 0, 0);
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
    private void doHandleEvent(Event event, boolean statePause, /*String sensorType,*/
                               boolean forRestartEvents, /*boolean manualRestart,*/
                               boolean forDelayStartAlarm, boolean forDelayEndAlarm,
                               Profile mergedProfile, DataWrapper dataWrapper)
    {
        if (PhoneProfilesService.displayPreferencesErrorNotification(null, event, context)) {
            event.setStatus(Event.ESTATUS_STOP);
            return;
        }

        startProfileMerged = false;
        endProfileMerged = false;

        int newEventStatus;// = Event.ESTATUS_NONE;

        notAllowedTime = false;
        notAllowedBattery = false;
        notAllowedCall = false;
        notAllowedAccessory = false;
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
        notAllowedSoundProfile = false;

        timePassed = true;
        batteryPassed = true;
        callPassed = true;
        accessoryPassed = true;
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
        soundProfilePassed = true;

//        if (PPApplication.logEnabled()) {
//            if (forRestartEvents) {
//                PPApplication.logE("[FIFO_TEST] EventsHandler.doHandleEvents", "--- start --------------------------");
//                PPApplication.logE("[FIFO_TEST] EventsHandler.doHandleEvents", "------- event._id=" + event._id);
//                PPApplication.logE("[FIFO_TEST] EventsHandler.doHandleEvents", "------- event._name=" + event._name);
//                PPApplication.logE("[FIFO_TEST] EventsHandler.doHandleEvents", "------- sensorType=" + sensorType);
//            }
//        }

        event._eventPreferencesTime.doHandleEvent(this/*, forRestartEvents*/);
        event._eventPreferencesBattery.doHandleEvent(this/*, sensorType, forRestartEvents*/);
        event._eventPreferencesCall.doHandleEvent(this/*, forRestartEvents*/);
        event._eventPreferencesAccessories.doHandleEvent(this/*, forRestartEvents*/);
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
        event._eventPreferencesSoundProfile.doHandleEvent(this/*, forRestartEvents*/);

//        if (PPApplication.logEnabled()) {
//            PPApplication.logE("[FIFO_TEST] ----- EventsHandler.doHandleEvent", "event._eventPreferencesTime._enabled=" + event._eventPreferencesTime._enabled);
//            PPApplication.logE("[FIFO_TEST] ----- EventsHandler.doHandleEvent", "notAllowedTime=" + notAllowedTime);
//            PPApplication.logE("[FIFO_TEST] ----- EventsHandler.doHandleEvent", "timePassed=" + timePassed);
//        }

        boolean allPassed = true;
        boolean someNotAllowed = false;
        boolean anySensorEnabled = false;
        if (event._eventPreferencesTime._enabled) {
            anySensorEnabled = true;
            if (!notAllowedTime)
                //noinspection ConstantConditions
                allPassed &= timePassed;
            else
                someNotAllowed = true;
        }
        if (event._eventPreferencesBattery._enabled) {
            anySensorEnabled = true;
            if (!notAllowedBattery)
                allPassed &= batteryPassed;
            else
                someNotAllowed = true;
        }
        if (event._eventPreferencesCall._enabled) {
            anySensorEnabled = true;
            if (!notAllowedCall)
                allPassed &= callPassed;
            else
                someNotAllowed = true;
        }
        if (event._eventPreferencesAccessories._enabled) {
            anySensorEnabled = true;
            if (!notAllowedAccessory)
                allPassed &= accessoryPassed;
            else
                someNotAllowed = true;
        }
        if (event._eventPreferencesCalendar._enabled) {
            anySensorEnabled = true;
            if (!notAllowedCalendar)
                allPassed &= calendarPassed;
            else
                someNotAllowed = true;
        }
        if (event._eventPreferencesWifi._enabled) {
            anySensorEnabled = true;
            if (!notAllowedWifi)
                allPassed &= wifiPassed;
            else
                someNotAllowed = true;
        }
        if (event._eventPreferencesScreen._enabled) {
            anySensorEnabled = true;
            if (!notAllowedScreen)
                allPassed &= screenPassed;
            else
                someNotAllowed = true;
        }
        if (event._eventPreferencesBluetooth._enabled) {
            anySensorEnabled = true;
            if (!notAllowedBluetooth)
                allPassed &= bluetoothPassed;
            else
                someNotAllowed = true;
        }
        if (event._eventPreferencesSMS._enabled) {
            anySensorEnabled = true;
            if (!notAllowedSms)
                allPassed &= smsPassed;
            else
                someNotAllowed = true;
        }
        if (event._eventPreferencesNotification._enabled) {
            anySensorEnabled = true;
            if (!notAllowedNotification)
                allPassed &= notificationPassed;
            else
                someNotAllowed = true;
        }
        if (event._eventPreferencesApplication._enabled) {
            anySensorEnabled = true;
            if (!notAllowedApplication)
                allPassed &= applicationPassed;
            else
                someNotAllowed = true;
        }
        if (event._eventPreferencesLocation._enabled) {
            anySensorEnabled = true;
            if (!notAllowedLocation)
                allPassed &= locationPassed;
            else
                someNotAllowed = true;
        }
        if (event._eventPreferencesOrientation._enabled) {
            anySensorEnabled = true;
            if (!notAllowedOrientation)
                allPassed &= orientationPassed;
            else
                someNotAllowed = true;
        }
        if (event._eventPreferencesMobileCells._enabled) {
            anySensorEnabled = true;
            if (!notAllowedMobileCell)
                allPassed &= mobileCellPassed;
            else
                someNotAllowed = true;
        }
        if (event._eventPreferencesNFC._enabled) {
            anySensorEnabled = true;
            if (!notAllowedNfc)
                allPassed &= nfcPassed;
            else
                someNotAllowed = true;
        }
        if (event._eventPreferencesRadioSwitch._enabled) {
            anySensorEnabled = true;
            if (!notAllowedRadioSwitch)
                allPassed &= radioSwitchPassed;
            else
                someNotAllowed = true;
        }
        if (event._eventPreferencesAlarmClock._enabled) {
            anySensorEnabled = true;
            if (!notAllowedAlarmClock)
                allPassed &= alarmClockPassed;
            else
                someNotAllowed = true;
        }
        if (event._eventPreferencesDeviceBoot._enabled) {
            anySensorEnabled = true;
            if (!notAllowedDeviceBoot)
                allPassed &= deviceBootPassed;
            else
                someNotAllowed = true;
        }
        if (event._eventPreferencesSoundProfile._enabled) {
            anySensorEnabled = true;
            if (!notAllowedSoundProfile)
                allPassed &= soundProfilePassed;
            else
                someNotAllowed = true;
        }

        if (!anySensorEnabled) {
            // force set event as paused
            allPassed = false;
            someNotAllowed = false;
        }

//        if (PPApplication.logEnabled()) {
//            if (forRestartEvents && someNotAllowed) {
//                PPApplication.logE("[FIFO_TEST] EventsHandler.doHandleEvents", "timePassed=" + timePassed);
//                PPApplication.logE("[FIFO_TEST] EventsHandler.doHandleEvents", "batteryPassed=" + batteryPassed);
//                PPApplication.logE("[FIFO_TEST] EventsHandler.doHandleEvents", "callPassed=" + callPassed);
//                PPApplication.logE("[FIFO_TEST] EventsHandler.doHandleEvents", "accessoryPassed=" + accessoryPassed);
//                PPApplication.logE("[FIFO_TEST] EventsHandler.doHandleEvents", "calendarPassed=" + calendarPassed);
//                PPApplication.logE("[FIFO_TEST] EventsHandler.doHandleEvents", "wifiPassed=" + wifiPassed);
//                PPApplication.logE("[FIFO_TEST] EventsHandler.doHandleEvents", "screenPassed=" + screenPassed);
//                PPApplication.logE("[FIFO_TEST] EventsHandler.doHandleEvents", "bluetoothPassed=" + bluetoothPassed);
//                PPApplication.logE("[FIFO_TEST] EventsHandler.doHandleEvents", "smsPassed=" + smsPassed);
//                PPApplication.logE("[FIFO_TEST] EventsHandler.doHandleEvents", "notificationPassed=" + notificationPassed);
//                PPApplication.logE("[FIFO_TEST] EventsHandler.doHandleEvents", "applicationPassed=" + applicationPassed);
//                PPApplication.logE("[FIFO_TEST] EventsHandler.doHandleEvents", "locationPassed=" + locationPassed);
//                PPApplication.logE("[FIFO_TEST] EventsHandler.doHandleEvents", "orientationPassed=" + orientationPassed);
//                PPApplication.logE("[FIFO_TEST] EventsHandler.doHandleEvents", "mobileCellPassed=" + mobileCellPassed);
//                PPApplication.logE("[FIFO_TEST] EventsHandler.doHandleEvents", "nfcPassed=" + nfcPassed);
//                PPApplication.logE("[FIFO_TEST] EventsHandler.doHandleEvents", "radioSwitchPassed=" + radioSwitchPassed);
//                PPApplication.logE("[FIFO_TEST] EventsHandler.doHandleEvents", "alarmClockPassed=" + alarmClockPassed);
//
//                PPApplication.logE("[FIFO_TEST] EventsHandler.doHandleEvents", "notAllowedTime=" + notAllowedTime);
//                PPApplication.logE("[FIFO_TEST] EventsHandler.doHandleEvents", "notAllowedBattery=" + notAllowedBattery);
//                PPApplication.logE("[FIFO_TEST] EventsHandler.doHandleEvents", "notAllowedCall=" + notAllowedCall);
//                PPApplication.logE("[FIFO_TEST] EventsHandler.doHandleEvents", "notAllowedAccessory=" + notAllowedAccessory);
//                PPApplication.logE("[FIFO_TEST] EventsHandler.doHandleEvents", "notAllowedCalendar=" + notAllowedCalendar);
//                PPApplication.logE("[FIFO_TEST] EventsHandler.doHandleEvents", "notAllowedWifi=" + notAllowedWifi);
//                PPApplication.logE("[FIFO_TEST] EventsHandler.doHandleEvents", "notAllowedScreen=" + notAllowedScreen);
//                PPApplication.logE("[FIFO_TEST] EventsHandler.doHandleEvents", "notAllowedBluetooth=" + notAllowedBluetooth);
//                PPApplication.logE("[FIFO_TEST] EventsHandler.doHandleEvents", "notAllowedSms=" + notAllowedSms);
//                PPApplication.logE("[FIFO_TEST] EventsHandler.doHandleEvents", "notAllowedNotification=" + notAllowedNotification);
//                PPApplication.logE("[FIFO_TEST] EventsHandler.doHandleEvents", "notAllowedApplication=" + notAllowedApplication);
//                PPApplication.logE("[FIFO_TEST] EventsHandler.doHandleEvents", "notAllowedLocation=" + notAllowedLocation);
//                PPApplication.logE("[FIFO_TEST] EventsHandler.doHandleEvents", "notAllowedOrientation=" + notAllowedOrientation);
//                PPApplication.logE("[FIFO_TEST] EventsHandler.doHandleEvents", "notAllowedMobileCell=" + notAllowedMobileCell);
//                PPApplication.logE("[FIFO_TEST] EventsHandler.doHandleEvents", "notAllowedNfc=" + notAllowedNfc);
//                PPApplication.logE("[FIFO_TEST] EventsHandler.doHandleEvents", "notAllowedRadioSwitch=" + notAllowedRadioSwitch);
//                PPApplication.logE("[FIFO_TEST] EventsHandler.doHandleEvents", "notAllowedAlarmClock=" + notAllowedAlarmClock);
//                PPApplication.logE("[FIFO_TEST] EventsHandler.doHandleEvents", "-----------------------------------");
//            }
//        }

//            if (event._name.equals("Event")) {
//        if (forRestartEvents) {
//            PPApplication.logE("[FIFO_TEST] ----- EventsHandler.doHandleEvents", "allPassed=" + allPassed);
//            PPApplication.logE("[FIFO_TEST] ----- EventsHandler.doHandleEvents", "someNotAllowed=" + someNotAllowed);
//        }
//
//            if (event._name.equals("Event")) {
//                //PPApplication.logE("EventsHandler.doHandleEvents","eventStart="+eventStart);
//                PPApplication.logE("[***] EventsHandler.doHandleEvents", "forRestartEvents=" + forRestartEvents);
//                PPApplication.logE("[***] EventsHandler.doHandleEvents", "statePause=" + statePause);
//            }
        //}

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

//            if (PPApplication.logEnabled()) {
////                if (event._name.equals("Event")) {
//                if (forRestartEvents) {
//                    PPApplication.logE("[FIFO_TEST] EventsHandler.doHandleEvents", "event._name=" + event._name);
//                    PPApplication.logE("[FIFO_TEST] EventsHandler.doHandleEvents", "event.getStatus()=" + event.getStatus());
//                    PPApplication.logE("[FIFO_TEST] EventsHandler.doHandleEvents", "newEventStatus=" + newEventStatus);
//                }
//            }

            //PPApplication.logE("@@@ EventsHandler.doHandleEvents","restartEvent="+restartEvent);

            if ((event.getStatus() != newEventStatus) || forRestartEvents || event._isInDelayStart || event._isInDelayEnd) {
//                if (event._name.equals("Event"))
//                if (forRestartEvents) {
//                    PPApplication.logE("[FIFO_TEST] EventsHandler.doHandleEvents", " do new event status");
//                    PPApplication.logE("[FIFO_TEST] EventsHandler.doHandleEvents", "event._delayStart="+event._delayStart);
//                    PPApplication.logE("[FIFO_TEST] EventsHandler.doHandleEvents", "event._delayEnd="+event._delayEnd);
//                    PPApplication.logE("[FIFO_TEST] EventsHandler.doHandleEvents", "event._isInDelayStart="+event._isInDelayStart);
//                    PPApplication.logE("[FIFO_TEST] EventsHandler.doHandleEvents", "event._isInDelayEnd="+event._isInDelayEnd);
//                }

                if (((newEventStatus == Event.ESTATUS_RUNNING) || forRestartEvents) && (!statePause)) {
                    // do start of events, all sensors are passed

                    boolean continueHandle = true;
                    if (newEventStatus == Event.ESTATUS_PAUSE) {
                        // is paused, for this do not start it
                        continueHandle = false;
                    }

                    boolean isInDelayEnd = false;
                    if (continueHandle) {
                        if (event._isInDelayEnd) {
                            // is in dealy end, for this is already running
//                            if (forRestartEvents)
//                                PPApplication.logE("[FIFO_TEST] EventsHandler.doHandleEvents", "_isInDelayEnd");

                            // remove delay end because is already running
                            event.removeDelayEndAlarm(dataWrapper);

                            // do not start, because is already running
                            isInDelayEnd = true;
                        }
                    }

//                    if (forRestartEvents)
//                        PPApplication.logE("[FIFO_TEST] ----- EventsHandler.doHandleEvents", "start - continueHandle="+continueHandle);

                    if (!continueHandle) {
                        //noinspection ConstantConditions
//                        if (forRestartEvents)
//                            PPApplication.logE("[FIFO_TEST] EventsHandler.doHandleEvents","--- end --------------------------");
                        return;
                    }

//                    if (PPApplication.logEnabled()) {
////                        if (event._name.equals("Event")) {
//                        if (forRestartEvents) {
//                            PPApplication.logE("[FIFO_TEST] ----- EventsHandler.doHandleEvents", "start event");
//                            PPApplication.logE("[FIFO_TEST] ----- EventsHandler.doHandleEvents", "event._name=" + event._name);
//                        }
//                    }

                    if ((!isInDelayEnd) || forRestartEvents) {
//                        if (event._name.equals("Event"))
//                        if (forRestartEvents)
//                            PPApplication.logE("[FIFO_TEST] ----- EventsHandler.doHandleEvents", "start event (2)");
                        if (!forDelayStartAlarm) {
//                            if (event._name.equals("Event"))
//                            if (forRestartEvents)
//                                PPApplication.logE("[FIFO_TEST] ----- EventsHandler.doHandleEvents", "start event (3)");
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
//                            if (event._name.equals("Event"))
//                            if (forRestartEvents)
//                                PPApplication.logE("[FIFO_TEST] ----- EventsHandler.doHandleEvents", "event._isInDelayStart=" + event._isInDelayStart);
                            if (!event._isInDelayStart) {
                                // no delay alarm is set
                                // start event
                                long oldMergedProfile = mergedProfile._id;
                                //Profile _oldMergedProfile = mergedProfile;
//                                if (forRestartEvents)
//                                    PPApplication.logE("[FIFO_TEST] ----- EventsHandler.doHandleEvents", "call startEvent() (1)");
                                event.startEvent(dataWrapper, /*interactive,*/ forRestartEvents, mergedProfile);
                                startProfileMerged = oldMergedProfile != mergedProfile._id;
//                                if (event._name.equals("Event")) {
//                                if (forRestartEvents) {
//                                    //PPApplication.logE("[***] EventsHandler.doHandleEvents", "_oldMergedProfile="+_oldMergedProfile._name);
//                                    PPApplication.logE("[FIFO_TEST] ----- EventsHandler.doHandleEvents", "mergedProfile._id="+mergedProfile._id);
//                                    PPApplication.logE("[FIFO_TEST] ----- EventsHandler.doHandleEvents", "mergedProfile._name="+mergedProfile._name);
//                                    PPApplication.logE("[FIFO_TEST] ----- EventsHandler.doHandleEvents", "startProfileMerged="+startProfileMerged);
//                                }
                            }
                        }
                        if (forDelayStartAlarm && event._isInDelayStart) {
//                            if (event._name.equals("Event"))
//                            if (forRestartEvents)
//                                PPApplication.logE("[FIFO_TEST] ----- EventsHandler.doHandleEvents", "start event (4)");
                            // called for delay alarm
                            // start event
                            long oldMergedProfile = mergedProfile._id;
//                            if (forRestartEvents)
//                                PPApplication.logE("[FIFO_TEST] ----- EventsHandler.doHandleEvents", "call startEvent() (2)");
                            event.startEvent(dataWrapper, /*interactive,*/ forRestartEvents, mergedProfile);
                            startProfileMerged = oldMergedProfile != mergedProfile._id;
//                            if (event._name.equals("Event")) {
//                            if (forRestartEvents) {
////                                PPApplication.logE("[***] EventsHandler.doHandleEvents", "oldMergedProfile="+oldMergedProfile);
//                                PPApplication.logE("[FIFO_TEST] ----- EventsHandler.doHandleEvents", "mergedProfile._id="+mergedProfile._id);
//                                PPApplication.logE("[FIFO_TEST] ----- EventsHandler.doHandleEvents", "startProfileMerged="+startProfileMerged);
//                            }
                        }
                    }
                }
                if (((newEventStatus == Event.ESTATUS_PAUSE) || forRestartEvents) && statePause) {
                    // do end of events, some sensors are not passed
                    // when pausing and it is for restart events (forRestartEvent=true), force pause

                    boolean isInDelayStart = false;
                    if (event._isInDelayStart) {
                        // is in delay start, for this is already paused

                        //if (event._name.equals("Event"))
//                        if (forRestartEvents)
//                            PPApplication.logE("[FIFO_TEST] EventsHandler.doHandleEvents", "isInDelayStart");

                        // remove delay start because is already paused
                        event.removeDelayStartAlarm(dataWrapper);

                        // do not pause, because is already paused
                        isInDelayStart = true;
                    }

//                    if (PPApplication.logEnabled()) {
//                        //if (event._name.equals("Event")) {
//                        if (forRestartEvents) {
//                            PPApplication.logE("[FIFO_TEST] EventsHandler.doHandleEvents", "pause event");
//                            PPApplication.logE("[FIFO_TEST] EventsHandler.doHandleEvents", "event._name=" + event._name);
//                        }
//                    }

                    if ((!isInDelayStart) || forRestartEvents) {
//                        if (forRestartEvents)
//                            PPApplication.logE("[FIFO_TEST] EventsHandler.doHandleEvents", "end event (2)");
                        if (!forDelayEndAlarm) {
//                            if (forRestartEvents)
//                                PPApplication.logE("[FIFO_TEST] EventsHandler.doHandleEvents", "end event (3)");
                            //if (event._name.equals("Event"))
                            //    PPApplication.logE("[***] EventsHandler.doHandleEvents", "!forDelayEndAlarm");
                            if (!event._isInDelayEnd) {
                                // if not delay alarm is set, set it
                                // this also set event._isInDelayEnd
                                event.setDelayEndAlarm(dataWrapper, forRestartEvents); // for end delay
                            }
                            if (event._isInDelayEnd) {
                                // if delay expires, pause event
                                // this also set event._isInDelayEnd
                                event.checkDelayEnd();
                            }
//                            if (forRestartEvents)
//                                PPApplication.logE("[FIFO_TEST] EventsHandler.doHandleEvents", "event._isInDelayEnd=" + event._isInDelayEnd);
                            if (!event._isInDelayEnd) {
                                // no delay alarm is set
                                // pause event
                                long oldMergedProfile = mergedProfile._id;
//                                if (forRestartEvents)
//                                    PPApplication.logE("[FIFO_TEST] EventsHandler.doHandleEvents", "call pauseEvent() (1)");

                                // do not allow restart events in Event.doActivateEndProfile() when is already doing restart events
                                // allowRestart parameter must be false for doing restart events (to avoid infinite loop)
                                event.pauseEvent(dataWrapper, true, false,
                                        false, true, mergedProfile, !forRestartEvents, forRestartEvents, true);

                                endProfileMerged = oldMergedProfile != mergedProfile._id;
//                                if (event._name.equals("Event")) {
//                                if (forRestartEvents) {
////                                    PPApplication.logE("[***] EventsHandler.doHandleEvents", "oldMergedProfile="+oldMergedProfile);
//                                    PPApplication.logE("[FIFO_TEST] EventsHandler.doHandleEvents", "mergedProfile._id="+mergedProfile._id);
//                                    PPApplication.logE("[FIFO_TEST] EventsHandler.doHandleEvents", "endProfileMerged="+endProfileMerged);
//                                }
                            }
                        }

                        if (forRestartEvents && event._isInDelayEnd) {
                            // do not use delay end alarm for restart events
                            event.removeDelayEndAlarm(dataWrapper);
                        }
                        if (forDelayEndAlarm && event._isInDelayEnd) {
//                            if (forRestartEvents)
//                                PPApplication.logE("[FIFO_TEST] EventsHandler.doHandleEvents", "end event (4)");
                            // called for delay alarm
                            // pause event
                            long oldMergedProfile = mergedProfile._id;
//                            if (forRestartEvents)
//                                PPApplication.logE("[FIFO_TEST] EventsHandler.doHandleEvents", "call pauseEvent() (2)");
                            event.pauseEvent(dataWrapper, true, false,
                                    false, true, mergedProfile, !forRestartEvents, forRestartEvents, true);
                            endProfileMerged = oldMergedProfile != mergedProfile._id;
//                            if (event._name.equals("Event")) {
//                            if (forRestartEvents) {
////                                PPApplication.logE("[***] EventsHandler.doHandleEvents", "oldMergedProfile="+oldMergedProfile);
//                                PPApplication.logE("[FIFO_TEST] EventsHandler.doHandleEvents", "mergedProfile._id="+mergedProfile._id);
//                                PPApplication.logE("[FIFO_TEST] EventsHandler.doHandleEvents", "endProfileMerged="+endProfileMerged);
//                            }
                        }
                    }
                }
            }
        }

//        if (forRestartEvents)
//            PPApplication.logE("[FIFO_TEST] EventsHandler.doHandleEvents","--- end --------------------------");
    }

//--------


    void setEventSMSParameters(String phoneNumber, long date, int simSlot) {
        eventSMSPhoneNumber = phoneNumber;
        eventSMSDate = date;
        eventSMSFromSIMSlot = simSlot;
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

    void setEventCallParameters(int callEventType, String phoneNumber, long eventTime, int simSlot) {
        EventPreferencesCall.setEventCallEventType(context, callEventType);
        EventPreferencesCall.setEventCallEventTime(context, eventTime);
        EventPreferencesCall.setEventCallPhoneNumber(context, phoneNumber);
        EventPreferencesCall.setEventCallFromSIMSlot(context, simSlot);
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

        //noinspection Java8ListSort
        Collections.sort(eventList, new PriorityComparator());
    }

}