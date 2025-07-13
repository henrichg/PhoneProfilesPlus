package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.telephony.TelephonyManager;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/** @noinspection ExtractMethodRecommender*/
class EventsHandler {

    final Context context;

    int[] sensorType;

    private int oldRingerMode;
    //private int oldSystemRingerMode;
    private int oldZenMode;

    //private String oldRingtone;
    //private String oldRingtoneSIM1;
    //private String oldRingtoneSIM2;

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
    private String eventApplicationPackageName;
    private long eventApplicationDate;
    //private String eventCallControlPhoneNumber;
    //private long eventCallControlDate;

    private boolean startProfileMerged;
    private boolean endProfileMerged;

    boolean notAllowedTime;
    boolean notAllowedBattery;
    boolean notAllowedCall;
    boolean notAllowedAccessory;
    boolean notAllowedCalendar;
    boolean notAllowedWifi;
    boolean notAllowedScreen;
    boolean notAllowedBrightness;
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
    boolean notAllowedPeriodic;
    boolean notAllowedVolumes;
    boolean notAllowedActivatedProfile;
    boolean notAllowedRoaming;
    boolean notAllowedVPN;
    boolean notAllowedMusic;
    boolean notAllowedCallControl;

    boolean timePassed;
    boolean batteryPassed;
    boolean callPassed;
    boolean accessoryPassed;
    boolean calendarPassed;
    boolean wifiPassed;
    boolean screenPassed;
    boolean brightnessPassed;
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
    boolean periodicPassed;
    boolean volumesPassed;
    boolean activatedProfilePassed;
    boolean roamingPassed;
    boolean vpnPassed;
    boolean musicPassed;
    boolean callControlPassed;

    static final int SENSOR_TYPE_RADIO_SWITCH = 1;
    static final int SENSOR_TYPE_RESTART_EVENTS = 2;
    static final int SENSOR_TYPE_RESTART_EVENTS_NOT_UNBLOCK = 3;
    static final int SENSOR_TYPE_MANUAL_RESTART_EVENTS = 4;
    static final int SENSOR_TYPE_PHONE_CALL = 5;
    static final int SENSOR_TYPE_CALENDAR_PROVIDER_CHANGED = 6;
    static final int SENSOR_TYPE_SEARCH_CALENDAR_EVENTS = 7;
    static final int SENSOR_TYPE_SMS = 8;
    static final int SENSOR_TYPE_NOTIFICATION = 9;
    static final int SENSOR_TYPE_NFC_TAG = 10;
    static final int SENSOR_TYPE_EVENT_DELAY_START = 11;
    static final int SENSOR_TYPE_EVENT_DELAY_END = 12;
    static final int SENSOR_TYPE_BATTERY = 13;
    static final int SENSOR_TYPE_BATTERY_WITH_LEVEL = 14;
    static final int SENSOR_TYPE_BLUETOOTH_CONNECTION = 15;
    static final int SENSOR_TYPE_BLUETOOTH_STATE = 16;
    static final int SENSOR_TYPE_DOCK_CONNECTION = 17;
    static final int SENSOR_TYPE_CALENDAR = 18;
    static final int SENSOR_TYPE_TIME = 19;
    static final int SENSOR_TYPE_APPLICATION = 20;
    static final int SENSOR_TYPE_HEADSET_CONNECTION = 21;
    //static final int SENSOR_TYPE_NOTIFICATION_EVENT_END = 22;
    static final int SENSOR_TYPE_SMS_EVENT_END = 23;
    static final int SENSOR_TYPE_WIFI_CONNECTION = 24;
    static final int SENSOR_TYPE_WIFI_STATE = 25;
    static final int SENSOR_TYPE_POWER_SAVE_MODE = 26;
    static final int SENSOR_TYPE_LOCATION_SCANNER = 27;
    static final int SENSOR_TYPE_LOCATION_MODE = 28;
    static final int SENSOR_TYPE_DEVICE_ORIENTATION = 29;
    static final int SENSOR_TYPE_MOBILE_CELLS = 30;
    static final int SENSOR_TYPE_NFC_EVENT_END = 31;
    static final int SENSOR_TYPE_WIFI_SCANNER = 32;
    static final int SENSOR_TYPE_BLUETOOTH_SCANNER = 33;
    static final int SENSOR_TYPE_SCREEN = 34;
    static final int SENSOR_TYPE_DEVICE_IDLE_MODE = 35;
    static final int SENSOR_TYPE_PHONE_CALL_EVENT_END = 36;
    static final int SENSOR_TYPE_ALARM_CLOCK = 37;
    static final int SENSOR_TYPE_ALARM_CLOCK_EVENT_END = 38;
    static final int SENSOR_TYPE_DEVICE_BOOT = 39;
    static final int SENSOR_TYPE_DEVICE_BOOT_EVENT_END = 40;
    static final int SENSOR_TYPE_PERIODIC_EVENTS_HANDLER = 41;
    static final int SENSOR_TYPE_ACCESSORIES = 42;
    static final int SENSOR_TYPE_CALENDAR_EVENT_EXISTS_CHECK = 43;
    static final int SENSOR_TYPE_CONTACTS_CACHE_CHANGED = 44;
    static final int SENSOR_TYPE_SOUND_PROFILE = 45;
    static final int SENSOR_TYPE_PERIODIC = 46;
    static final int SENSOR_TYPE_PERIODIC_EVENT_END = 47;
    static final int SENSOR_TYPE_VOLUMES = 48;
    static final int SENSOR_TYPE_ACTIVATED_PROFILE = 49;
    static final int SENSOR_TYPE_ROAMING = 50;
    static final int SENSOR_TYPE_VPN = 51;
    static final int SENSOR_TYPE_SIM_STATE_CHANGED = 52;
    static final int SENSOR_TYPE_BOOT_COMPLETED = 53;
    static final int SENSOR_TYPE_BRIGHTNESS = 54;
    static final int SENSOR_TYPE_APPLICATION_EVENT_END = 55;
    static final int SENSOR_TYPE_MUSIC = 56;
    static final int SENSOR_TYPE_CALL_CONTROL = 57;
    static final int SENSOR_TYPE_CALL_CONTROL_EVENT_END = 58;
    static final int SENSOR_TYPE_ACTIVATED_PROFILE_EVENT_END = 59;
    static final int SENSOR_TYPE_ALL = 999;

    EventsHandler(Context context) {
        this.context = context.getApplicationContext();
    }

    void handleEvents(int[] sensorType) {
//        PPApplicationStatic.logE("[SYNCHRONIZED] EventsHandler.handleEvents", "PPApplication.eventsHandlerMutex");
        synchronized (PPApplication.eventsHandlerMutex) {
//            Log.e("EventsHandler.handleEvents", "(1) *****************");
            boolean manualRestart = Arrays.stream(sensorType).anyMatch(i -> i == SENSOR_TYPE_MANUAL_RESTART_EVENTS);
            boolean isRestart = (Arrays.stream(sensorType).anyMatch(i -> i == SENSOR_TYPE_RESTART_EVENTS)) || manualRestart;

            if (!PPApplicationStatic.getApplicationStarted(true, true))
                // application is not started
                return;

            /*
            PhoneProfilesService ppService;

            if (PhoneProfilesService.getInstance() != null) {
                ppService = PhoneProfilesService.getInstance();
            }
            else
                return;
            */

            this.sensorType = sensorType;

//            if ((sensorType == SENSOR_TYPE_LOCATION_SCANNER))
//                PPApplicationStatic.logE("[IN_EVENTS_HANDLER] EventsHandler.handleEvents", "------ do EventsHandler, sensorType="+sensorType+" ------");

            // save ringer mode, zen mode, ringtone before handle events, (must be from system!)
            // used by ringing call simulation (in doEndHandler())
            //oldRingerMode = ApplicationPreferences.prefRingerMode;
            //oldZenMode = ApplicationPreferences.prefZenMode;
            AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
            if (audioManager != null) {
                switch (audioManager.getRingerMode()) {
                    case AudioManager.RINGER_MODE_SILENT:
                        oldRingerMode = Profile.RINGERMODE_SILENT;
//                    PPApplicationStatic.logE("[RINGING_SIMULATION] EventsHandler.handleEvents", "oldRingerMode=SILENT");
                        break;
                    case AudioManager.RINGER_MODE_VIBRATE:
//                    PPApplicationStatic.logE("[RINGING_SIMULATION] EventsHandler.handleEvents", "oldRingerMode=VIBRATE");
                        oldRingerMode = Profile.RINGERMODE_VIBRATE;
                        break;
                    //case AudioManager.RINGER_MODE_NORMAL:
                    default:
                        oldRingerMode = Profile.RINGERMODE_RING;
//                    PPApplicationStatic.logE("[RINGING_SIMULATION] EventsHandler.handleEvents", "oldRingerMode=RING");
                        break;
                }
            } else
                oldRingerMode = Profile.RINGERMODE_RING;
            switch (ActivateProfileHelper.getSystemZenMode(context)) {
                case ActivateProfileHelper.SYSTEM_ZENMODE_ALARMS:
                    oldZenMode = Profile.ZENMODE_ALARMS;
//                    PPApplicationStatic.logE("[RINGING_SIMULATION] EventsHandler.handleEvents", "oldZenMode=ALARMS");
                    break;
                case ActivateProfileHelper.SYSTEM_ZENMODE_NONE:
                    oldZenMode = Profile.ZENMODE_NONE;
//                    PPApplicationStatic.logE("[RINGING_SIMULATION] EventsHandler.handleEvents", "oldZenMode=NONE");
                    break;
                case ActivateProfileHelper.SYSTEM_ZENMODE_PRIORITY:
                    oldZenMode = Profile.ZENMODE_PRIORITY;
//                    PPApplicationStatic.logE("[RINGING_SIMULATION] EventsHandler.handleEvents", "oldZenMode=PRIORITY");
                    break;
                //case ActivateProfileHelper.ZENMODE_ALL:
                default:
                    oldZenMode = Profile.ZENMODE_ALL;
//                    PPApplicationStatic.logE("[RINGING_SIMULATION] EventsHandler.handleEvents", "oldZenMode=ALL");
                    break;
            }

            /*
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
                        String _uri = ActivateProfileHelper.getRingtoneFromSystem(appContext, 1);
                        if (_uri != null)
                            oldRingtoneSIM1 = _uri;
                        else
                            oldRingtoneSIM1 = oldRingtone;
                        _uri = ActivateProfileHelper.getRingtoneFromSystem(appContext, 2);
                        if (_uri != null)
                            oldRingtoneSIM2 = _uri;
                        else
                            oldRingtoneSIM2 = oldRingtone;
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
            */

            if (!EventStatic.getGlobalEventsRunning(context)) {
                // events are globally stopped

                doEndHandler(null, null);
                //dataWrapper.invalidateDataWrapper();

                return;
            }

            if ((DatabaseHandler.getInstance(context.getApplicationContext()).getNotStoppedEventsCount() == 0) &&
                    (!manualRestart)){
                // not any event is paused or running
                PPApplicationStatic.setApplicationFullyStarted(context);
//                PPApplicationStatic.logE("[APPLICATION_FULLY_STARTED] EventsHandler.handleEvents", "(1)");

                doEndHandler(null, null);

                return;
            }

            boolean _continue = false;
            for (int _sensorType : sensorType) {
                if (!alwaysEnabledSensor(_sensorType)) {
                    // _sensorType is not always sensor
                    // check existence of sensors
                    int eventType = getEventTypeForSensor(_sensorType);
                    if (DatabaseHandler.getInstance(context.getApplicationContext()).getTypeEventsCount(eventType) != 0) {
                        // event type exists
                        _continue = true;
                    }
                } else {
                    // _sensorType is always sensor
                    _continue = true;
                }
            }
            if (!_continue) {
//                if ((sensorType == SENSOR_TYPE_BATTERY) || (sensorType == SENSOR_TYPE_BATTERY_WITH_LEVEL))
//                PPApplicationStatic.logE("[IN_EVENTS_HANDLER] EventsHandler.handleEvents", "------ events not exists ------");

                PPApplicationStatic.setApplicationFullyStarted(context);
//                PPApplicationStatic.logE("[APPLICATION_FULLY_STARTED] EventsHandler.handleEvents", "(2)");

                doEndHandler(null, null);

                //if (isRestart) {
                //    PPApplication.updateGUI(/*context, true, true*/);
                //}
                //else {
                //    PPApplication.updateGUI(/*context, true, false*/);
                //}

                return;
            }

            PPApplicationStatic.getProfileBeforeActivation(context.getApplicationContext());
//            Log.e("EventsHandler.handleEvents", "getProfileBeforeActivation="+PPApplication.prefProfileBeforeActivation);

            DataWrapper dataWrapper = new DataWrapper(context.getApplicationContext(), false, 0, false, 0, 0, 0f);
            dataWrapper.fillEventList();
            dataWrapper.fillEventTimelineList();
            dataWrapper.fillProfileList(false, false);

            // pause all events for Restart events
            if (isRestart)
                dataWrapper.pauseAllEvents(false, false, false, true, true, manualRestart);

// ---- Special for sensors which requires calendar data - START -----------
            boolean saveCalendarStartEndTime = false;
            if (isRestart) {
                if (EventStatic.isEventPreferenceAllowed(EventPreferencesCalendar.PREF_EVENT_CALENDAR_ENABLED, false, context.getApplicationContext()).preferenceAllowed ==
                        PreferenceAllowed.PREFERENCE_ALLOWED) {
                    for (Event _event : dataWrapper.eventList) {
                        if ((_event.getStatus() != Event.ESTATUS_STOP) &&
                                (_event._eventPreferencesCalendar._enabled)) {
                            saveCalendarStartEndTime = true;
                            break;
                        }
                    }
                }
            }

            if (Arrays.stream(sensorType).anyMatch(i ->
                    (i == SENSOR_TYPE_CALENDAR_PROVIDER_CHANGED) ||
                    (i == SENSOR_TYPE_SEARCH_CALENDAR_EVENTS) ||
                    (i == SENSOR_TYPE_CALENDAR) ||
                    (i == SENSOR_TYPE_CALENDAR_EVENT_EXISTS_CHECK))
                    || saveCalendarStartEndTime) {
                // search for calendar events
                for (Event _event : dataWrapper.eventList) {
                    if ((_event._eventPreferencesCalendar._enabled) && (_event.getStatus() != Event.ESTATUS_STOP)) {
                        if (_event._eventPreferencesCalendar.isRunnable(context) &&
                            _event._eventPreferencesCalendar.isAllConfigured(context)) {
                            _event._eventPreferencesCalendar.saveCalendarEventExists(dataWrapper);
                            _event._eventPreferencesCalendar.saveStartEndTime(dataWrapper);
                        }
                    }
                }
            }
// ---- Special for sensors which requires calendar data - END -----------

            if (isRestart) {
                // for restart events, set startTime to 0
                dataWrapper.clearSensorsStartTime();
            } else {
                if (Arrays.stream(sensorType).anyMatch(i ->
                        (i == SENSOR_TYPE_SMS) ||
                        (i == SENSOR_TYPE_CONTACTS_CACHE_CHANGED))) {
                    // search for sms events, save start time
//                    PPApplicationStatic.logE("[CONTACTS_CACHE] EventsHandler.handleEvents", "(1) PPApplicationStatic.getContactsCache()");
                    ContactsCache contactsCache = PPApplicationStatic.getContactsCache();
                    if (contactsCache != null) {
                        List<Contact> contactList;
//                            PPApplicationStatic.logE("[SYNCHRONIZED] EventPreferencesCall.doHandleEvent", "PPApplication.contactsCacheMutex");
//                        PPApplicationStatic.logE("[CONTACTS_CACHE] EventsHandler.handleEvents", "(1) contactsCache.getList()");
                        contactList = contactsCache.getList(/*false*/);
                        for (Event _event : dataWrapper.eventList) {
                            if (_event.getStatus() != Event.ESTATUS_STOP) {
                                if (_event._eventPreferencesSMS._enabled) {
                                    _event._eventPreferencesSMS.saveStartTime(contactList, dataWrapper, eventSMSPhoneNumber, eventSMSDate, eventSMSFromSIMSlot);
                                }
                            }
                        }
                        if (contactList != null)
                            contactList.clear();
                    }
                }

                if (Arrays.stream(sensorType).anyMatch(i -> i == SENSOR_TYPE_NFC_TAG)) {
                    // search for nfc events, save start time
                    for (Event _event : dataWrapper.eventList) {
                        if (_event.getStatus() != Event.ESTATUS_STOP) {
                            if (_event._eventPreferencesNFC._enabled) {
                                _event._eventPreferencesNFC.saveStartTime(dataWrapper, eventNFCTagName, eventNFCDate);
                            }
                        }
                    }
                }
                if (Arrays.stream(sensorType).anyMatch(i -> i == SENSOR_TYPE_APPLICATION)) {
                    // search for nfc events, save start time
                    for (Event _event : dataWrapper.eventList) {
                        if (_event.getStatus() != Event.ESTATUS_STOP) {
                            if (_event._eventPreferencesApplication._enabled) {
                                _event._eventPreferencesApplication.saveStartTime(dataWrapper, eventApplicationPackageName, eventApplicationDate);
                            }
                        }
                    }
                }
                if (Arrays.stream(sensorType).anyMatch(i ->
                        (i == SENSOR_TYPE_PHONE_CALL) ||
                        (i == SENSOR_TYPE_CONTACTS_CACHE_CHANGED))) {
                    // search for call events, save start time
//                    PPApplicationStatic.logE("[CONTACTS_CACHE] EventsHandler.handleEvents", "(2) PPApplicationStatic.getContactsCache()");
                    ContactsCache contactsCache = PPApplicationStatic.getContactsCache();
                    if (contactsCache != null) {
                        List<Contact> contactList;
//                            PPApplicationStatic.logE("[SYNCHRONIZED] EventPreferencesCall.doHandleEvent", "PPApplication.contactsCacheMutex");
//                        PPApplicationStatic.logE("[CONTACTS_CACHE] EventsHandler.handleEvents", "(2) contactsCache.getList()");
                        contactList = contactsCache.getList(/*false*/);
                        for (Event _event : dataWrapper.eventList) {
                            if (_event.getStatus() != Event.ESTATUS_STOP) {
                                if (_event._eventPreferencesCall._enabled) {
                                    if ((_event._eventPreferencesCall._callEvent == EventPreferencesCall.CALL_EVENT_MISSED_CALL) ||
                                                (_event._eventPreferencesCall._callEvent == EventPreferencesCall.CALL_EVENT_INCOMING_CALL_ENDED) ||
                                                (_event._eventPreferencesCall._callEvent == EventPreferencesCall.CALL_EVENT_OUTGOING_CALL_ENDED))
                                        _event._eventPreferencesCall.saveRunAfterCallEndTime(contactList, dataWrapper);
                                }
                            }
                        }
                        if (contactList != null)
                            contactList.clear();
                    }
                }
                if (Arrays.stream(sensorType).anyMatch(i -> i == SENSOR_TYPE_ALARM_CLOCK)) {
                    // search for alarm clock events, save start time
                    for (Event _event : dataWrapper.eventList) {
                        if (_event.getStatus() != Event.ESTATUS_STOP) {
                            if (_event._eventPreferencesAlarmClock._enabled) {
                                _event._eventPreferencesAlarmClock.saveStartTime(dataWrapper, eventAlarmClockDate, eventAlarmClockPackageName);
                            }
                        }
                    }
                }
                if (Arrays.stream(sensorType).anyMatch(i -> i == SENSOR_TYPE_DEVICE_BOOT)) {
                    // search for device boot events, save start time
                    for (Event _event : dataWrapper.eventList) {
                        if (_event.getStatus() != Event.ESTATUS_STOP) {
                            if (_event._eventPreferencesDeviceBoot._enabled) {
                                _event._eventPreferencesDeviceBoot.saveStartTime(dataWrapper, eventDeviceBootDate);
                            }
                        }
                    }
                }

                if (Arrays.stream(sensorType).anyMatch(i -> i == SENSOR_TYPE_PERIODIC_EVENTS_HANDLER)) {
                    // search for periodic events, save start time
                    for (Event _event : dataWrapper.eventList) {
                        if (_event.getStatus() != Event.ESTATUS_STOP) {
                            if (_event._eventPreferencesPeriodic._enabled) {
                                _event._eventPreferencesPeriodic.increaseCounter(dataWrapper);
                            }
                        }
                    }
                }
                if (Arrays.stream(sensorType).anyMatch(i -> i == SENSOR_TYPE_PERIODIC)) {
                    // search for periodic events, save start time
                    for (Event _event : dataWrapper.eventList) {
                        if (_event.getStatus() != Event.ESTATUS_STOP) {
                            if (_event._eventPreferencesPeriodic._enabled) {
                                _event._eventPreferencesPeriodic.saveStartTime(dataWrapper);
                            }
                        }
                    }
                }
                if (Arrays.stream(sensorType).anyMatch(i ->
                        (i == SENSOR_TYPE_CALL_CONTROL) ||
                        (i == SENSOR_TYPE_CONTACTS_CACHE_CHANGED))) {
                    // search for sms events, save start time
//                    PPApplicationStatic.logE("[CONTACTS_CACHE] EventsHandler.handleEvents", "(3) PPApplicationStatic.getContactsCache()");
                    ContactsCache contactsCache = PPApplicationStatic.getContactsCache();
                    if (contactsCache != null) {
                        List<Contact> contactList;
//                            PPApplicationStatic.logE("[SYNCHRONIZED] EventPreferencesCallControl.doHandleEvent", "PPApplication.contactsCacheMutex");
//                        PPApplicationStatic.logE("[CONTACTS_CACHE] EventsHandler.handleEvents", "(3) contactsCache.getList()");
                        contactList = contactsCache.getList(/*false*/);
                        for (Event _event : dataWrapper.eventList) {
                            if (_event.getStatus() != Event.ESTATUS_STOP) {
                                if (_event._eventPreferencesCallControl._enabled) {
                                    EventPreferencesCallControl.getEventCallControlTime(context);
                                    EventPreferencesCallControl.getEventCallControlPhoneNumber(context);
                                    EventPreferencesCallControl.getEventCallControlCallDirection(context);
                                    _event._eventPreferencesCallControl.saveStartTime(contactList, dataWrapper,
                                            ApplicationPreferences.prefEventCallControlPhoneNumber,
                                            ApplicationPreferences.prefEventCallControlTime);
                                }
                            }
                        }
                        if (contactList != null)
                            contactList.clear();
                    }
                }

                if (Arrays.stream(sensorType).anyMatch(i -> i == SENSOR_TYPE_ACTIVATED_PROFILE)) {
                    // search for nfc events, save start time
                    for (Event _event : dataWrapper.eventList) {
                        if (_event.getStatus() != Event.ESTATUS_STOP) {
                            if (_event._eventPreferencesActivatedProfile._enabled) {
                                _event._eventPreferencesActivatedProfile.saveStartTime(dataWrapper);
                            }
                        }
                    }
                }

            }

            boolean forDelayStartAlarm = Arrays.stream(sensorType).anyMatch(i -> i == SENSOR_TYPE_EVENT_DELAY_START);
            boolean forDelayEndAlarm = Arrays.stream(sensorType).anyMatch(i -> i == SENSOR_TYPE_EVENT_DELAY_END);

            // no refresh notification and widgets
            PPApplication.lockRefresh = true;

            Profile mergedProfile = DataWrapperStatic.getNonInitializedProfile("", "", 0);

            int mergedProfilesCount = 0;
            int usedEventsCount = 0;

            Profile oldActivatedProfile = dataWrapper.getActivatedProfileFromDB(false, false);
            boolean profileChanged = false;

            //boolean notified = false;

            List<EventTimeline> eventTimelineList = dataWrapper.getEventTimelineList(false);

            sortEventsByStartOrderDesc(dataWrapper.eventList);
            Event notifiedPausedEvent = null;
            if (isRestart) {

                // 1. pause events
                for (Event _event : dataWrapper.eventList) {

                    if (_event.getStatus() != Event.ESTATUS_STOP) {
                        // only pause events
                        // pause also paused events

                        boolean running = _event.getStatus() == Event.ESTATUS_RUNNING;
                        doHandleEvent(_event, true, true, manualRestart, false, false, mergedProfile, dataWrapper);
                        boolean paused = _event.getStatus() == Event.ESTATUS_PAUSE;

                        if (running && paused) {
                            if ((_event._notificationSoundEnd != null) &&
                                (!_event._notificationSoundEnd.isEmpty()) ||
                                _event._notificationVibrateEnd)
                                notifiedPausedEvent = _event;

                            if (startProfileMerged)
                                mergedProfilesCount++;
                            if (endProfileMerged)
                                mergedProfilesCount++;
                            if (startProfileMerged || endProfileMerged)
                                usedEventsCount++;
                        }

                    }
                }
                if (notifiedPausedEvent != null) {
                    // notify this event
                    notifiedPausedEvent.notifyEventEnd(context, /*true,*/ true);
                    //notified = true;
                }

//                PPApplicationStatic.logE("[SYNCHRONIZED] EventsHandler.handleEvents", "PPApplication.profileActivationMutex");
                synchronized (PPApplication.profileActivationMutex) {
                    List<String> activateProfilesFIFO = new ArrayList<>();
                    dataWrapper.fifoSaveProfiles(activateProfilesFIFO);
                }

                // 2. start events
                //sortEventsByStartOrderAsc(dataWrapper.eventList);
                Event notifiedStartedEvent = null;
                Collections.reverse(dataWrapper.eventList);
                for (Event _event : dataWrapper.eventList) {

                    if (_event.getStatus() != Event.ESTATUS_STOP) {
                        // only start events

                        // start all events
                        boolean paused = _event.getStatus() == Event.ESTATUS_PAUSE;
                        doHandleEvent(_event, false, true, manualRestart, false, false, mergedProfile, dataWrapper);
                        boolean running = _event.getStatus() == Event.ESTATUS_RUNNING;

                        if (running && paused) {
                            if ((_event._notificationSoundStart != null) &&
                                    (!_event._notificationSoundStart.isEmpty()) ||
                                    _event._notificationVibrateStart)
                                notifiedStartedEvent = _event;

                            if (startProfileMerged)
                                mergedProfilesCount++;
                            if (endProfileMerged)
                                mergedProfilesCount++;
                            if (startProfileMerged || endProfileMerged)
                                usedEventsCount++;
                        }

                    }
                }
                if (notifiedStartedEvent != null) {
                    // notify this event;
                    notifiedStartedEvent.notifyEventStart(context, /*true,*/ true);
                    //notified = true;
                }

            } else {

                //1. pause events
                for (Event _event : dataWrapper.eventList) {

                    if (_event.getStatus() != Event.ESTATUS_STOP) {
                        // only pause events

                        boolean running = _event.getStatus() == Event.ESTATUS_RUNNING;
                        doHandleEvent(_event, true, false, false, forDelayStartAlarm, forDelayEndAlarm, mergedProfile, dataWrapper);
                        boolean paused = _event.getStatus() == Event.ESTATUS_PAUSE;

                        if (running && paused) {
                            // pause only running events
                            if ((_event._notificationSoundEnd != null) &&
                                    (!_event._notificationSoundEnd.isEmpty()) ||
                                    _event._notificationVibrateEnd)
                                notifiedPausedEvent = _event;

                            if (startProfileMerged)
                                mergedProfilesCount++;
                            if (endProfileMerged)
                                mergedProfilesCount++;
                            if (startProfileMerged || endProfileMerged)
                                usedEventsCount++;
                        }

                    }
                }
                if (notifiedPausedEvent != null) {
                    // notify this event;
                    notifiedPausedEvent.notifyEventEnd(context, /*true,*/ true);
                    //notified = true;
                }

                //2. start events
                Event notifiedStartedEvent = null;
                Collections.reverse(dataWrapper.eventList);
                for (Event _event : dataWrapper.eventList) {

                    if (_event.getStatus() != Event.ESTATUS_STOP) {
                        // only start events

                        boolean paused = _event.getStatus() == Event.ESTATUS_PAUSE;
                        doHandleEvent(_event, false, false, false, forDelayStartAlarm, forDelayEndAlarm, mergedProfile, dataWrapper);
                        boolean running = _event.getStatus() == Event.ESTATUS_RUNNING;

                        if (running && paused) {
                            // start only paused events
                            if ((_event._notificationSoundStart != null) &&
                                    (!_event._notificationSoundStart.isEmpty()) ||
                                    _event._notificationVibrateStart)
                                notifiedStartedEvent = _event;

                            if (startProfileMerged)
                                mergedProfilesCount++;
                            if (endProfileMerged)
                                mergedProfilesCount++;
                            if (startProfileMerged || endProfileMerged)
                                usedEventsCount++;
                        }

                    }
                }
                if (notifiedStartedEvent != null) {
                    // notify this event;
                    notifiedStartedEvent.notifyEventStart(context, /*true,*/ true);
                    //notified = true;
                }
            }

            PPApplication.lockRefresh = false;

            //if ((!restartAtEndOfEvent) || isRestart) {
            //    // No any paused events has "Restart events" at end of event

            //////////////////
            //// when no events are running or manual activation,
            //// activate background profile when no profile is activated

            // get running events count
            int runningEventCountE = eventTimelineList.size();

            // activated profile may be changed, when event has enabled manual profile activation
            Profile semiOldActivatedProfile = dataWrapper.getActivatedProfileFromDB(false, false);
            long semiOldActivatedProfileId = 0;
            // activate default profile only when semiOldActovatedProfile do not have set Duration or alarm ends
            boolean semiOldHasDuration = false;
            if (semiOldActivatedProfile != null) {
                semiOldActivatedProfileId = semiOldActivatedProfile._id;
//                Log.e("EventsHandler.handleEvents", "semiOldActivatedProfileId="+semiOldActivatedProfileId);
//                Log.e("EventsHandler.handleEvents", "semiOldActivatedProfileId._name="+semiOldActivatedProfile._name);
//                Log.e("EventsHandler.handleEvents", "semiOldActivatedProfileId._duration="+semiOldActivatedProfile._duration);
//                Log.e("EventsHandler.handleEvents", "semiOldActivatedProfileId._askForDuration="+semiOldActivatedProfile._askForDuration);

                boolean alarmEnds = true;
                if (ApplicationPreferences.prefActivatedProfileEndDurationTime.get(semiOldActivatedProfileId) != null) {
                    //noinspection DataFlowIssue
                    long endDurationTime = ApplicationPreferences.prefActivatedProfileEndDurationTime.get(semiOldActivatedProfileId);
                    alarmEnds = endDurationTime == 0;
//                    Log.e("EventsHandler.handleEvents", "alarmEnds="+alarmEnds);
                }
                semiOldHasDuration =
                        ((semiOldActivatedProfile._endOfActivationType == Profile.AFTER_DURATION_DURATION_TYPE_DURATION) && ((semiOldActivatedProfile._duration != 0)) ||
                         (semiOldActivatedProfile._endOfActivationType == Profile.AFTER_DURATION_DURATION_TYPE_EXACT_TIME)) &&
                        (!alarmEnds) &&
                        (!semiOldActivatedProfile._askForDuration);
            }
//            Log.e("EventsHandler.handleEvents", "semiOldHasDuration="+semiOldHasDuration);

            long defaultProfileId = Profile.PROFILE_NO_ACTIVATE;
            boolean notifyDefaultProfile = false;
            boolean isAnyEventEnabled = DatabaseHandler.getInstance(context.getApplicationContext()).isAnyEventEnabled();

            if (!semiOldHasDuration) {

                if (!DataWrapperStatic.getIsManualProfileActivation(false, context)) {
                    // no manual profile activation

//                    Log.e("EventsHandler.handleEvents", "no manual activation");
//                    Log.e("EventsHandler.handleEvents", "runningEventCountE="+runningEventCountE);

                    if (runningEventCountE == 0) {
                        // activate default profile

                        // no events running

                        // THIS MUST BE PURE DEFAULT PROFILE, BECAUSE IT IS TESTED
                        defaultProfileId = ApplicationPreferences.applicationDefaultProfile;

                        if ((defaultProfileId != Profile.PROFILE_NO_ACTIVATE) && isAnyEventEnabled) {
                            defaultProfileId = ApplicationPreferences.getApplicationDefaultProfileOnBoot();

                            // is not currently activated profile with duration
                            // then is possible to activate default profile
                            boolean defaultProfileActivated = false;
                            if ((semiOldActivatedProfileId == 0) ||
                                    isRestart ||
                                    (semiOldActivatedProfileId != defaultProfileId)) {
                                mergedProfile.mergeProfiles(defaultProfileId, dataWrapper/*, false*/);
                                notifyDefaultProfile = true;

                                defaultProfileActivated = true;
                                mergedProfilesCount++;

                                dataWrapper.fifoAddProfile(defaultProfileId, 0);
                            }

                            if (((semiOldActivatedProfileId == defaultProfileId) &&
                                    ((mergedProfilesCount > 0) || defaultProfileActivated)) ||
                                    (isRestart && (!manualRestart))) {
                                // block interactive parameters when
                                // - activated profile is default profile
                                // - it is not manual restart of events
                                //
                                // this is set, because is not good to again execute interactive parameters
                                // for already activated default profile
                                PPApplicationStatic.setBlockProfileEventActions(true);
                            }

                        } else {
                            if (PPApplication.prefLastActivatedProfile != 0) {
                                dataWrapper.fifoAddProfile(PPApplication.prefLastActivatedProfile, 0);
                            }
                        }
                    }
                } else {
                    // manual profile activation

//                    Log.e("EventsHandler.handleEvents", "manual activation=");

                    boolean defaultProfileActivated = false;

                    if (semiOldActivatedProfileId > 0) {
                        // any profile activated, set back semi-old, this uses profile activated by events

                        //noinspection ConstantConditions
                        defaultProfileId = Profile.PROFILE_NO_ACTIVATE;
                        mergedProfile.mergeProfiles(semiOldActivatedProfileId, dataWrapper/*, false*/);
                        //mergedProfilesCount++;

                        //dataWrapper.fifoAddProfile(semiOldActivatedProfileId, 0);
                    } else {
                        // not any profile activated

                        defaultProfileId = ApplicationPreferences.getApplicationDefaultProfileOnBoot();

                        if ((defaultProfileId != Profile.PROFILE_NO_ACTIVATE) && isAnyEventEnabled) {
                            // if not any profile activated, activate default profile
                            notifyDefaultProfile = true;
                            mergedProfile.mergeProfiles(defaultProfileId, dataWrapper/*, false*/);

                            defaultProfileActivated = true;
                            mergedProfilesCount++;

                            dataWrapper.fifoAddProfile(defaultProfileId, 0);
                        } else {
                            if (PPApplication.prefLastActivatedProfile != 0) {
                                dataWrapper.fifoAddProfile(PPApplication.prefLastActivatedProfile, 0);
                            }
                        }

                        if (isAnyEventEnabled) {
                            if (((semiOldActivatedProfileId == defaultProfileId) &&
                                    ((mergedProfilesCount > 0) || defaultProfileActivated)) ||
                                    (isRestart && (!manualRestart))) {
                                // block interactive parameters when
                                // - activated profile is default profile
                                // - it is not manual restart of events
                                //
                                // this is set, because is not good to again execute interactive parameters
                                // for already activated default profile
                                PPApplicationStatic.setBlockProfileEventActions(true);
                            }
                        }
                    }
                }
            }
            ////////////////

            String defaultProfileNotificationSound = "";
            boolean defaultProfileNotificationVibrate = false;

            if ((defaultProfileId != Profile.PROFILE_NO_ACTIVATE) && isAnyEventEnabled && notifyDefaultProfile) {
                // only when activated is background profile, play event notification sound

                defaultProfileNotificationSound = ApplicationPreferences.applicationDefaultProfileNotificationSound;
                defaultProfileNotificationVibrate = ApplicationPreferences.applicationDefaultProfileNotificationVibrate;
            }

            //boolean doSleep = false;

            if (mergedProfile._id != 0) {
                // activate merged profile
                DatabaseHandler.getInstance(context.getApplicationContext()).saveMergedProfile(mergedProfile);

                // check if profile has changed
                if (!mergedProfile.compareProfile(oldActivatedProfile))
                    profileChanged = true;

                if (profileChanged || (usedEventsCount > 0) || isRestart /*sensorType.equals(SENSOR_TYPE_MANUAL_RESTART_EVENTS)*/) {

                    // log only when merged profile is not the same as last activated or for restart events
                    PPApplicationStatic.addActivityLog(context, PPApplication.ALTYPE_MERGED_PROFILE_ACTIVATION,
                            null,
                            DataWrapperStatic.getProfileNameWithManualIndicatorAsString(mergedProfile, true, "", false, false, false, false, dataWrapper),
                            mergedProfilesCount + StringConstants.CHAR_HARD_SPACE +"["+StringConstants.CHAR_HARD_SPACE + usedEventsCount + StringConstants.CHAR_HARD_SPACE + "]");

                    // do not save profile to fifo - because it is merged profile
                    // profile is alrady added by Event.startEvent(), Event.doActivateEndProfile()
                    // or added in this method (default profile...)
                    dataWrapper.activateProfileFromEvent(/*0,*/ mergedProfile._id, false, true,
                            isRestart, manualRestart, true);
                    // wait for profile activation
                    //doSleep = true;
                }
            }

            //if (!notified) {
                // notify default profile
                if (!defaultProfileNotificationSound.isEmpty() || defaultProfileNotificationVibrate) {
                    PlayRingingNotification.playNotificationSound(
                            defaultProfileNotificationSound,
                            defaultProfileNotificationVibrate,
                            false, context);
                    //notified = true;
                }
            //}

            //if (doSleep || notified) {
            //    PPApplication.sleep(500);
            //}

            doEndHandler(dataWrapper, mergedProfile);

            PPApplicationStatic.setApplicationFullyStarted(context);
//            PPApplicationStatic.logE("[APPLICATION_FULLY_STARTED] EventsHandler.handleEvents", "(3)");

            // refresh all GUI - must be for restart scanners
            if (profileChanged || (usedEventsCount > 0) || isRestart /*sensorType.equals(SENSOR_TYPE_MANUAL_RESTART_EVENTS)*/) {
//                PPApplicationStatic.logE("[PPP_NOTIFICATION] EventsHandler.handleEvents", "call of updateGUI");
                PPApplication.updateGUI(false, false, context);
            }
            else {
                // refresh only Editor
//                PPApplicationStatic.logE("[LOCAL_BROADCAST_CALL] EventsHandler.handleEvents", "xxx");
                Intent refreshIntent = new Intent(PPApplication.ACTION_REFRESH_EDITOR_GUI_BROADCAST_RECEIVER);
                refreshIntent.putExtra(RefreshActivitiesBroadcastReceiver.EXTRA_REFRESH_ICONS, false);
                //refreshIntent.putExtra(PPApplication.EXTRA_PROFILE_ID, profileId);
                //refreshIntent.putExtra(PPApplication.EXTRA_EVENT_ID, eventId);
                LocalBroadcastManager.getInstance(context).sendBroadcast(refreshIntent);
            }

            dataWrapper.invalidateDataWrapper();

//                PPApplicationStatic.logE("[IN_EVENTS_HANDLER] EventsHandler.handleEvents", "-- end --------------------------------");

        }
    }

    private boolean alwaysEnabledSensor(int sensorType) {
        switch (sensorType) {
            case SENSOR_TYPE_SCREEN:
            case SENSOR_TYPE_BRIGHTNESS:
                // call doHandleEvents for all screen on/off changes
            case SENSOR_TYPE_PERIODIC_EVENTS_HANDLER:
            case SENSOR_TYPE_RESTART_EVENTS:
            case SENSOR_TYPE_MANUAL_RESTART_EVENTS:
            case SENSOR_TYPE_EVENT_DELAY_START:
            case SENSOR_TYPE_EVENT_DELAY_END:
            case SENSOR_TYPE_DEVICE_IDLE_MODE:
            case SENSOR_TYPE_SIM_STATE_CHANGED:
            case SENSOR_TYPE_BOOT_COMPLETED:
            case SENSOR_TYPE_CONTACTS_CACHE_CHANGED:
                return true;
        }
        return false;
    }

    private int getEventTypeForSensor(int sensorType) {
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
            case SENSOR_TYPE_ACCESSORIES:
                return DatabaseHandler.ETYPE_ACCESSORY;
            case SENSOR_TYPE_TIME:
                return DatabaseHandler.ETYPE_TIME;
            case SENSOR_TYPE_APPLICATION:
            case SENSOR_TYPE_APPLICATION_EVENT_END:
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
            case SENSOR_TYPE_ACTIVATED_PROFILE:
            case SENSOR_TYPE_ACTIVATED_PROFILE_EVENT_END:
                return DatabaseHandler.ETYPE_ACTIVATED_PROFILE;
            case SENSOR_TYPE_ROAMING:
                return DatabaseHandler.ETYPE_ROAMING;
            case SENSOR_TYPE_VPN:
                return DatabaseHandler.ETYPE_VPN;
            case SENSOR_TYPE_SOUND_PROFILE:
                return DatabaseHandler.ETYPE_SOUND_PROFILE;
            case SENSOR_TYPE_PERIODIC:
            case SENSOR_TYPE_PERIODIC_EVENT_END:
                return DatabaseHandler.ETYPE_PERIODIC;
            case SENSOR_TYPE_VOLUMES:
                return DatabaseHandler.ETYPE_VOLUMES;
            case SENSOR_TYPE_SCREEN:
                return DatabaseHandler.ETYPE_SCREEN;
            case SENSOR_TYPE_BRIGHTNESS:
                return DatabaseHandler.ETYPE_BRIGHTNESS;
            case SENSOR_TYPE_MUSIC:
                return DatabaseHandler.ETYPE_MUSIC;
            case SENSOR_TYPE_CALL_CONTROL:
            case SENSOR_TYPE_CALL_CONTROL_EVENT_END:
                return DatabaseHandler.ETYPE_CALL_CONTROL;
            default:
                return DatabaseHandler.ETYPE_ALL;
        }
    }

    private void doEndHandler(DataWrapper dataWrapper, Profile mergedProfile) {

        if (Arrays.stream(sensorType).anyMatch(i -> i == SENSOR_TYPE_PHONE_CALL) && (dataWrapper != null)) {
            TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

            // doEndHandler is called even if no event exists, but ringing call simulation is only for running event with call sensor
            boolean inRinging = false;
            if (telephony != null) {
                int callState = GlobalUtils.getCallState(context);
                inRinging = (callState == TelephonyManager.CALL_STATE_RINGING);
            }
            if (inRinging) {
                // start PhoneProfilesService for ringing call simulation
                try {
                    boolean simulateRingingCall = false;
                    if (ApplicationPreferences.applicationSimulateRingingCall) {
                        String phoneNumber = ApplicationPreferences.prefEventCallPhoneNumber;

//                    PPApplicationStatic.logE("[CONTACTS_CACHE] EventsHandler.doEndHandler", "PPApplicationStatic.getContactsCache()");
                        ContactsCache contactsCache = PPApplicationStatic.getContactsCache();
                        if (contactsCache != null) {
                            List<Contact> contactList;
//                        PPApplicationStatic.logE("[SYNCHRONIZED] EventsHandler.doEndHandler", "PPApplication.contactsCacheMutex");
//                        PPApplicationStatic.logE("[CONTACTS_CACHE] EventsHandler.doEndHandler", "contactsCache.getList()");
                            contactList = contactsCache.getList(/*false*/);
                            for (Event _event : dataWrapper.eventList) {
                                if (_event._eventPreferencesCall._enabled && _event.getStatus() == Event.ESTATUS_RUNNING) {
                                    if (_event._eventPreferencesCall.isPhoneNumberConfigured(contactList, phoneNumber/*, dataWrapper*/)) {
                                        simulateRingingCall = true;
                                        break;
                                    }
                                }
                            }
                            if (contactList != null)
                                contactList.clear();
                        }
                    }
                    int simSlot = ApplicationPreferences.prefEventCallRunAfterCallEndFromSIMSlot;
//                    PPApplicationStatic.logE("[RINGING_SIMULATION] EventsHandler.doEndHandler", "simulateRingingCall="+simulateRingingCall);
                    if (simulateRingingCall && (!mergedProfile._volumeMuteSound)) {
                        // in profile is not enabled mute sound, do simulation

                        Intent commandIntent = new Intent(PhoneProfilesService.ACTION_COMMAND);
                        commandIntent.putExtra(PhoneProfilesService.EXTRA_SIMULATE_RINGING_CALL, true);
                        // add saved ringer mode, zen mode, ringtone before handle events as parameters
                        // ringing call simulator compare this with new (actual values), changed by currently activated profile

                        commandIntent.putExtra(PhoneProfilesService.EXTRA_OLD_RINGER_MODE, oldRingerMode);
                        //commandIntent.putExtra(PhoneProfilesService.EXTRA_OLD_SYSTEM_RINGER_MODE, oldSystemRingerMode);
                        commandIntent.putExtra(PhoneProfilesService.EXTRA_OLD_ZEN_MODE, oldZenMode);
                        //commandIntent.putExtra(PhoneProfilesService.EXTRA_OLD_RINGTONE, oldRingtone);
                        //commandIntent.putExtra(PhoneProfilesService.EXTRA_OLD_RINGTONE_SIM1, oldRingtoneSIM1);
                        //commandIntent.putExtra(PhoneProfilesService.EXTRA_OLD_RINGTONE_SIM2, oldRingtoneSIM2);
                        //commandIntent.putExtra(PhoneProfilesService.EXTRA_OLD_SYSTEM_RINGER_VOLUME, oldSystemRingerVolume);
                        commandIntent.putExtra(PhoneProfilesService.EXTRA_NEW_RINGER_MODE, mergedProfile._volumeRingerMode);
                        commandIntent.putExtra(PhoneProfilesService.EXTRA_NEW_ZEN_MODE, mergedProfile._volumeZenMode);
                        commandIntent.putExtra(PhoneProfilesService.EXTRA_NEW_RINGER_VOLUME, mergedProfile._volumeRingtone);
                        commandIntent.putExtra(PhoneProfilesService.EXTRA_NEW_RINTONE_CHANGE, mergedProfile._soundRingtoneChange);
                        commandIntent.putExtra(PhoneProfilesService.EXTRA_NEW_RINGTONE, mergedProfile._soundRingtone);
                        commandIntent.putExtra(PhoneProfilesService.EXTRA_NEW_RINTONE_CHANGE_SIM1, mergedProfile._soundRingtoneChangeSIM1);
                        commandIntent.putExtra(PhoneProfilesService.EXTRA_NEW_RINGTONE_SIM1, mergedProfile._soundRingtoneSIM1);
                        commandIntent.putExtra(PhoneProfilesService.EXTRA_NEW_RINTONE_CHANGE_SIM2, mergedProfile._soundRingtoneChangeSIM2);
                        commandIntent.putExtra(PhoneProfilesService.EXTRA_NEW_RINGTONE_SIM2, mergedProfile._soundRingtoneSIM2);
                        commandIntent.putExtra(PhoneProfilesService.EXTRA_CALL_FROM_SIM_SLOT, simSlot);

                        PPApplicationStatic.runCommand(context, commandIntent);
                    }
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                }
            }

            boolean inCall = false;
            if (telephony != null) {
                int callState = GlobalUtils.getCallState(context);

                inCall = (callState == TelephonyManager.CALL_STATE_RINGING) || (callState == TelephonyManager.CALL_STATE_OFFHOOK);
            }
            if (!inCall)
                setEventCallParameters(EventPreferencesCall.PHONE_CALL_EVENT_UNDEFINED, "", 0, 0);
        }
        else
        if (Arrays.stream(sensorType).anyMatch(i -> i == SENSOR_TYPE_PHONE_CALL_EVENT_END)) {
            setEventCallParameters(EventPreferencesCall.PHONE_CALL_EVENT_UNDEFINED, "", 0, 0);
        }
    }

//--------

    private void doHandleEvent(Event event, boolean statePause,
                               boolean forRestartEvents, boolean manualRestart,
                               boolean forDelayStartAlarm, boolean forDelayEndAlarm,
                               Profile mergedProfile, DataWrapper dataWrapper)
    {
        if (DataWrapperStatic.displayPreferencesErrorNotification(null, event, true, context)) {
            /* Do not stop event. For example user in profile disables Location, but event
               with WiFi sensor must stayed in running status.

                event.setStatus(Event.ESTATUS_STOP);
            */
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
        notAllowedBrightness = false;
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
        notAllowedPeriodic = false;
        notAllowedVolumes = false;
        notAllowedActivatedProfile = false;
        notAllowedRoaming = false;
        notAllowedVPN = false;
        notAllowedMusic = false;
        notAllowedCallControl = false;

        timePassed = true;
        batteryPassed = true;
        callPassed = true;
        accessoryPassed = true;
        calendarPassed = true;
        wifiPassed = true;
        screenPassed = true;
        brightnessPassed = true;
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
        periodicPassed = true;
        volumesPassed = true;
        activatedProfilePassed = true;
        roamingPassed = true;
        vpnPassed = true;
        musicPassed = true;
        callControlPassed = true;

        event._eventPreferencesTime.doHandleEvent(this/*, forRestartEvents*/);
        event._eventPreferencesBattery.doHandleEvent(this/*, sensorType, forRestartEvents*/);
        event._eventPreferencesCall.doHandleEvent(this/*, forRestartEvents*/);
        event._eventPreferencesAccessories.doHandleEvent(this/*, forRestartEvents*/);
        event._eventPreferencesCalendar.doHandleEvent(this/*, forRestartEvents*/);
        event._eventPreferencesWifi.doHandleEvent(this, forRestartEvents);
        event._eventPreferencesScreen.doHandleEvent(this/*, forRestartEvents*/);
        event._eventPreferencesBrightness.doHandleEvent(this/*, forRestartEvents*/);
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
        event._eventPreferencesPeriodic.doHandleEvent(this/*, forRestartEvents*/);
        event._eventPreferencesVolumes.doHandleEvent(this/*, forRestartEvents*/);
        event._eventPreferencesActivatedProfile.doHandleEvent(this/*, forRestartEvents*/);
        event._eventPreferencesRoaming.doHandleEvent(this/*, forRestartEvents*/);
        event._eventPreferencesVPN.doHandleEvent(this/*, forRestartEvents*/);
        event._eventPreferencesMusic.doHandleEvent(this/*, forRestartEvents*/);
        event._eventPreferencesCallControl.doHandleEvent(this/*, forRestartEvents*/);

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
        if (event._eventPreferencesBrightness._enabled) {
            anySensorEnabled = true;
            if (!notAllowedBrightness)
                allPassed &= brightnessPassed;
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
        if (event._eventPreferencesPeriodic._enabled) {
            anySensorEnabled = true;
            if (!notAllowedPeriodic)
                allPassed &= periodicPassed;
            else
                someNotAllowed = true;
        }
        if (event._eventPreferencesVolumes._enabled) {
            anySensorEnabled = true;
            if (!notAllowedVolumes)
                allPassed &= volumesPassed;
            else
                someNotAllowed = true;
        }
        if (event._eventPreferencesActivatedProfile._enabled) {
            anySensorEnabled = true;
            if (!notAllowedActivatedProfile)
                allPassed &= activatedProfilePassed;
            else
                someNotAllowed = true;
        }
        if (event._eventPreferencesRoaming._enabled) {
            anySensorEnabled = true;
            if (!notAllowedRoaming)
                allPassed &= roamingPassed;
            else
                someNotAllowed = true;
        }
        if (event._eventPreferencesVPN._enabled) {
            anySensorEnabled = true;
            if (!notAllowedVPN)
                allPassed &= vpnPassed;
            else
                someNotAllowed = true;
        }
        if (event._eventPreferencesMusic._enabled) {
            anySensorEnabled = true;
            if (!notAllowedMusic)
                allPassed &= musicPassed;
            else
                someNotAllowed = true;
        }
        if (event._eventPreferencesCallControl._enabled) {
            anySensorEnabled = true;
            if (!notAllowedCallControl)
                allPassed &= callControlPassed;
            else
                someNotAllowed = true;
        }

        if (!anySensorEnabled) {
            // force set event as paused
            allPassed = false;
            //noinspection ConstantConditions
            someNotAllowed = false;
        }

        if (!someNotAllowed) {
            // some sensor is not allowed, do not change event status

            if (allPassed) {
                // all sensors are passed

                newEventStatus = Event.ESTATUS_RUNNING;

            } else
                newEventStatus = Event.ESTATUS_PAUSE;

            if ((event.getStatus() != newEventStatus) || forRestartEvents || event._isInDelayStart || event._isInDelayEnd) {

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

                            // remove delay end because is already running
                            event.removeDelayEndAlarm(dataWrapper);

                            // do not start, because is already running
                            isInDelayEnd = true;
                        }
                    }

                    if (!continueHandle) {
                        return;
                    }

                    if ((!isInDelayEnd) || forRestartEvents) {
                        if (!forDelayStartAlarm) {
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
                            if (!event._isInDelayStart) {
                                // no delay alarm is set
                                // start event
                                long oldMergedProfile = mergedProfile._id;
                                //Profile _oldMergedProfile = mergedProfile;
                                event.startEvent(dataWrapper, /*interactive,*/ forRestartEvents, manualRestart, mergedProfile);
                                startProfileMerged = oldMergedProfile != mergedProfile._id;
                            }
                        }
                        if (forDelayStartAlarm && event._isInDelayStart) {
                            // called for delay alarm
                            // start event
                            long oldMergedProfile = mergedProfile._id;
                            event.startEvent(dataWrapper, /*interactive,*/ forRestartEvents, manualRestart, mergedProfile);
                            startProfileMerged = oldMergedProfile != mergedProfile._id;
                        }
                    }
                }
                if (((newEventStatus == Event.ESTATUS_PAUSE) || forRestartEvents) && statePause) {
                    // do end of events, some sensors are not passed
                    // when pausing and it is for restart events (forRestartEvent=true), force pause

                    boolean isInDelayStart = false;
                    if (event._isInDelayStart) {
                        // is in delay start, for this is already paused

                        // remove delay start because is already paused
                        event.removeDelayStartAlarm(dataWrapper);

                        // do not pause, because is already paused
                        isInDelayStart = true;
                    }

                    if ((!isInDelayStart) || forRestartEvents) {
                        if (!forDelayEndAlarm) {
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
                            if (!event._isInDelayEnd) {
                                // no delay alarm is set
                                // pause event
                                long oldMergedProfile = mergedProfile._id;

                                // do not allow restart events in Event.doActivateEndProfile() when is already doing restart events
                                // allowRestart parameter must be false for doing restart events (to avoid infinite loop)
                                event.pauseEvent(dataWrapper, true, false,
                                        false, true, mergedProfile, !forRestartEvents, forRestartEvents, manualRestart, true);

                                endProfileMerged = oldMergedProfile != mergedProfile._id;
                            }
                        }

                        if (forRestartEvents && event._isInDelayEnd) {
                            // do not use delay end alarm for restart events
                            event.removeDelayEndAlarm(dataWrapper);
                        }
                        if (forDelayEndAlarm && event._isInDelayEnd) {
                            // called for delay alarm
                            // pause event
                            long oldMergedProfile = mergedProfile._id;
                            event.pauseEvent(dataWrapper, true, false,
                                    false, true, mergedProfile, !forRestartEvents, forRestartEvents, manualRestart, true);
                            endProfileMerged = oldMergedProfile != mergedProfile._id;
                        }
                    }
                }
            }
        }

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
        EventPreferencesCall.setEventCallEventTime(context, eventTime, callEventType);
        EventPreferencesCall.setEventCallPhoneNumber(context, phoneNumber);
        EventPreferencesCall.setEventCallFromSIMSlot(context, simSlot, callEventType);
    }

    void setEventApplicationParameters(String packageName, long date) {
        eventApplicationPackageName = packageName;
        eventApplicationDate = date;
    }

    void setEventDeviceBootParameters(long date) {
        eventDeviceBootDate = date;
    }

    void setEventCallControlParameters(String phoneNumber, long date, int direction) {
        EventPreferencesCallControl.setEventCallControlPhoneNumber(context, phoneNumber);
        EventPreferencesCallControl.setEventCallControlTime(context, date);
        EventPreferencesCallControl.setEventCallControlCallDirection(context, direction);
    }

    /*
    void sortEventsByStartOrderAsc(List<Event> eventList)
    {
        class PriorityComparator implements Comparator<Event> {
            public int compare(Event lhs, Event rhs) {
                int res = 0;
                if ((lhs != null) && (rhs != null))
                    res =  lhs._startOrder - rhs._startOrder;
                return res;
            }
        }

        eventList.sort(new PriorityComparator());
    }
    */

    private void sortEventsByStartOrderDesc(List<Event> eventList)
    {
        class StartOrderComparator implements Comparator<Event> {
            public int compare(Event lhs, Event rhs) {
                int res = 0;
                if ((lhs != null) && (rhs != null))
                    res =  rhs._startOrder - lhs._startOrder;
                return res;
            }
        }

        eventList.sort(new StartOrderComparator());
    }

}