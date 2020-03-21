package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.os.BatteryManager;
import android.os.Build;
import android.telephony.TelephonyManager;

import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

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

    private boolean startProfileMerged;
    private boolean endProfileMerged;

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

    public EventsHandler(Context context) {
        this.context = context;
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

                doEndHandler(dataWrapper, true);
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

            if (!eventsExists(sensorType, dataWrapper, false)) {
                // events not exists

                doEndHandler(dataWrapper, false);
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

            if (isRestart) {
                /*if (PPApplication.logEnabled()) {
                    PPApplication.logE("$$$ EventsHandler.handleEvents", "restart events");
                    PPApplication.logE("[DEFPROF] EventsHandler.handleEvents", "restart events");
                }*/

                //reactivateProfile = true;

                //oldActivatedProfile = null;

                // get running events count
                //List<EventTimeline> _etl = dataWrapper.getEventTimelineList(true);
                //runningEventCount0 = _etl.size();

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
                        doHandleEvents(_event, true, true, /*interactive,*/ false, false, /*reactivateProfile,*/ mergedProfile, sensorType, dataWrapper);
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
                        doHandleEvents(_event, false, true, /*interactive,*/ false, false, /*reactivateProfile,*/ mergedProfile, sensorType, dataWrapper);
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

                // get running events count
                //List<EventTimeline> _etl = dataWrapper.getEventTimelineList(true);
                //runningEventCount0 = _etl.size();

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
                        doHandleEvents(_event, true, false, /*interactive,*/ forDelayStartAlarm, forDelayEndAlarm, /*reactivateProfile,*/ mergedProfile, sensorType, dataWrapper);
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
                        doHandleEvents(_event, false, false, /*interactive,*/ forDelayStartAlarm, forDelayEndAlarm, /*true*//*reactivateProfile,*/ mergedProfile, sensorType, dataWrapper);
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

            //if (/*(!isRestart) &&*/ (runningEventCountE > runningEventCountP)) {
            // only running events is increased, play event notification sound

            //EventTimeline eventTimeline = eventTimelineList.get(runningEventCountE - 1);
            //notifyEventStart = dataWrapper.getEventById(eventTimeline._fkEvent);
            //}
            //else
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

                    PPApplication.addActivityLog(dataWrapper.context, PPApplication.ALTYPE_MERGED_PROFILE_ACTIVATION, null,
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
                    dataWrapper.updateNotificationAndWidgets(false, false);
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

            doEndHandler(dataWrapper, false);

            // refresh GUI
            Intent refreshIntent = new Intent(PPApplication.PACKAGE_NAME + ".RefreshActivitiesBroadcastReceiver");
            LocalBroadcastManager.getInstance(context).sendBroadcast(refreshIntent);


            //dataWrapper.invalidateDataWrapper();

            //PPApplication.logE("[TEST BATTERY] EventsHandler.handleEvents", "-- end --------------------------------");
        }
    }

    private boolean eventsExists(String sensorType, DataWrapper dataWrapper, boolean onlyRunning) {
        for (Event _event : dataWrapper.eventList) {
            boolean eventEnabled;
            if (onlyRunning)
                eventEnabled = _event.getStatus() == Event.ESTATUS_RUNNING;
            else
                eventEnabled = _event.getStatus() != Event.ESTATUS_STOP;
            if (eventEnabled) {
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

    private void doEndHandler(DataWrapper dataWrapper, boolean checkEventsExistence) {
        //PPApplication.logE("EventsHandler.doEndHandler","sensorType="+sensorType);
        //PPApplication.logE("EventsHandler.doEndHandler","callEventType="+callEventType);

        if (sensorType.equals(SENSOR_TYPE_PHONE_CALL)) {
            TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

            if ((!checkEventsExistence) || eventsExists(sensorType, dataWrapper, true)) {
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

//--------

    @SuppressLint({ "NewApi", "SimpleDateFormat" })
    private void doHandleEvents(Event event, boolean statePause,
                        boolean forRestartEvents, /*boolean interactive,*/
                        boolean forDelayStartAlarm, boolean forDelayEndAlarm,
            /*boolean reactivate,*/ Profile mergedProfile,
                        String sensorType, DataWrapper dataWrapper)
    {
        if (!EditorProfilesActivity.displayRedTextToPreferencesNotification(null, event, context)) {
            event.setStatus(Event.ESTATUS_STOP);
            return;
        }

        startProfileMerged = false;
        endProfileMerged = false;

        int newEventStatus;// = Event.ESTATUS_NONE;

        boolean notAllowedTime = false;
        boolean notAllowedBattery = false;
        boolean notAllowedCall = false;
        boolean notAllowedPeripheral = false;
        boolean notAllowedCalendar = false;
        boolean notAllowedWifi = false;
        boolean notAllowedScreen = false;
        boolean notAllowedBluetooth = false;
        boolean notAllowedSms = false;
        boolean notAllowedNotification = false;
        boolean notAllowedApplication = false;
        boolean notAllowedLocation = false;
        boolean notAllowedOrientation = false;
        boolean notAllowedMobileCell = false;
        boolean notAllowedNfc = false;
        boolean notAllowedRadioSwitch = false;
        boolean notAllowedAlarmClock = false;

        boolean timePassed = true;
        boolean batteryPassed = true;
        boolean callPassed = true;
        boolean peripheralPassed = true;
        boolean calendarPassed = true;
        boolean wifiPassed = true;
        boolean screenPassed = true;
        boolean bluetoothPassed = true;
        boolean smsPassed = true;
        boolean notificationPassed = true;
        boolean applicationPassed = true;
        boolean locationPassed = true;
        boolean orientationPassed = true;
        boolean mobileCellPassed = true;
        boolean nfcPassed = true;
        boolean radioSwitchPassed = true;
        boolean alarmClockPassed = true;

        /*if (PPApplication.logEnabled()) {
            PPApplication.logE("%%%%%%% EventsHandler.doHandleEvents", "--- start --------------------------");
            PPApplication.logE("%%%%%%% EventsHandler.doHandleEvents", "------- event._id=" + event._id);
            PPApplication.logE("%%%%%%% EventsHandler.doHandleEvents", "------- event._name=" + event._name);
            PPApplication.logE("%%%%%%% EventsHandler.doHandleEvents", "------- sensorType=" + sensorType);
        }*/

        if (event._eventPreferencesTime._enabled) {
            int oldSensorPassed = event._eventPreferencesTime.getSensorPassed();
            if ((Event.isEventPreferenceAllowed(EventPreferencesTime.PREF_EVENT_TIME_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED)
                // permissions are checked in EditorProfilesActivity.displayRedTextToPreferencesNotification()
                /*&& Permissions.checkEventLocation(context, event, null)*/) {
                /*boolean testEvent = (event._name != null) && event._name.equals("Plugged In Nighttime");
                if (testEvent) {
                    if (PPApplication.logEnabled()) {
                        PPApplication.logE("[TIME] EventsHandler.doHandleEvents", "------- event._id=" + event._id);
                        PPApplication.logE("[TIME] EventsHandler.doHandleEvents", "------- event._name=" + event._name);
                    }
                }*/

                // compute start datetime
                long startAlarmTime;
                long endAlarmTime;

                startAlarmTime = event._eventPreferencesTime.computeAlarm(true, context);
                endAlarmTime = event._eventPreferencesTime.computeAlarm(false, context);

                //if (startAlarmTime > 0) {
                //String alarmTimeS = DateFormat.getDateFormat(context).format(startAlarmTime) +
                //        " " + DateFormat.getTimeFormat(context).format(startAlarmTime);
                //if (testEvent)
                //    PPApplication.logE("[TIME] EventsHandler.doHandleEvents", "startAlarmTime=" + alarmTimeS);
                //}
                //else
                //if (testEvent)
                //    PPApplication.logE("[TIME] EventsHandler.doHandleEvents", "startAlarmTime=not alarm computed");
                //if (endAlarmTime > 0) {
                //String alarmTimeS = DateFormat.getDateFormat(context).format(endAlarmTime) +
                //        " " + DateFormat.getTimeFormat(context).format(endAlarmTime);
                //if (testEvent)
                //    PPApplication.logE("[TIME] EventsHandler.doHandleEvents", "endAlarmTime=" + alarmTimeS);
                //}
                //else
                //if (testEvent)
                //    PPApplication.logE("[TIME] EventsHandler.doHandleEvents", "endAlarmTime=not alarm computed");

                Calendar now = Calendar.getInstance();
                long nowAlarmTime = now.getTimeInMillis();
                //String alarmTimeS = DateFormat.getDateFormat(context).format(nowAlarmTime) +
                //        " " + DateFormat.getTimeFormat(context).format(nowAlarmTime);
                //if (testEvent)
                //    PPApplication.logE("[TIME] EventsHandler.doHandleEvents", "nowAlarmTime=" + alarmTimeS);

                /*boolean[] daysOfWeek =  new boolean[8];
                daysOfWeek[Calendar.SUNDAY] = event._eventPreferencesTime._sunday;
                daysOfWeek[Calendar.MONDAY] = event._eventPreferencesTime._monday;
                daysOfWeek[Calendar.TUESDAY] = event._eventPreferencesTime._tuesday;
                daysOfWeek[Calendar.WEDNESDAY] = event._eventPreferencesTime._wednesday;
                daysOfWeek[Calendar.THURSDAY] = event._eventPreferencesTime._thursday;
                daysOfWeek[Calendar.FRIDAY] = event._eventPreferencesTime._friday;
                daysOfWeek[Calendar.SATURDAY] = event._eventPreferencesTime._saturday;*/

                Calendar calStartTime = Calendar.getInstance();
                calStartTime.setTimeInMillis(startAlarmTime);
                //int startDayOfWeek = calStartTime.get(Calendar.DAY_OF_WEEK);
                //if (daysOfWeek[startDayOfWeek])
                //{
                // startTime of week is selected
                //if (testEvent)
                //    PPApplication.logE("[TIME] EventsHandler.doHandleEvents","startTime of week is selected");
                if ((startAlarmTime > 0) && (endAlarmTime > 0))
                    timePassed = ((nowAlarmTime >= startAlarmTime) && (nowAlarmTime < endAlarmTime));
                else
                    timePassed = false;
                /*}
                else {
                    PPApplication.logE("[TIME] EventsHandler.doHandleEvents","startTime of week is NOT selected");
                    timePassed = false;
                }*/

                //if (testEvent)
                //    PPApplication.logE("[TIME] EventsHandler.doHandleEvents", "timePassed=" + timePassed);

                if (!notAllowedTime) {
                    if (timePassed)
                        event._eventPreferencesTime.setSensorPassed(EventPreferences.SENSOR_PASSED_PASSED);
                    else
                        event._eventPreferencesTime.setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);
                }
            } else
                notAllowedTime = true;
            int newSensorPassed = event._eventPreferencesTime.getSensorPassed() & (~EventPreferences.SENSOR_PASSED_WAITING);
            if (oldSensorPassed != newSensorPassed) {
                event._eventPreferencesTime.setSensorPassed(newSensorPassed);
                DatabaseHandler.getInstance(context).updateEventSensorPassed(event, DatabaseHandler.ETYPE_TIME);
            }
        }

        if (event._eventPreferencesBattery._enabled) {
            int oldSensorPassed = event._eventPreferencesBattery.getSensorPassed();
            if (Event.isEventPreferenceAllowed(EventPreferencesBattery.PREF_EVENT_BATTERY_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                boolean isPowerSaveMode = DataWrapper.isPowerSaveMode(context);
                //PPApplication.logE("[BAT] EventsHandler.doHandleEvents", "isPowerSaveMode=" + isPowerSaveMode);

                boolean isCharging;
                int batteryPct;
                int plugged;

                // get battery status
                Intent batteryStatus = null;
                try { // Huawei devices: java.lang.IllegalArgumentException: registered too many Broadcast Receivers
                    IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                    batteryStatus = context.registerReceiver(null, filter);
                } catch (Exception ignored) {
                }

                if (batteryStatus != null) {
                    batteryPassed = false;

                    int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                    //PPApplication.logE("[BAT] EventsHandler.doHandleEvents", "status=" + status);
                    isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                            status == BatteryManager.BATTERY_STATUS_FULL;
                    //PPApplication.logE("[BAT] EventsHandler.doHandleEvents", "isCharging=" + isCharging);
                    plugged = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
                    //PPApplication.logE("[BAT] EventsHandler.doHandleEvents", "plugged=" + plugged);

                    int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                    int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                    /*if (PPApplication.logEnabled()) {
                        PPApplication.logE("[BAT] EventsHandler.doHandleEvents", "level=" + level);
                        PPApplication.logE("[BAT] EventsHandler.doHandleEvents", "scale=" + scale);
                    }*/

                    batteryPct = Math.round(level / (float) scale * 100);
                    //PPApplication.logE("[BAT] EventsHandler.doHandleEvents", "batteryPct=" + batteryPct);

                    if ((batteryPct >= event._eventPreferencesBattery._levelLow) &&
                            (batteryPct <= event._eventPreferencesBattery._levelHight))
                        batteryPassed = true;

                    if ((event._eventPreferencesBattery._charging > 0) ||
                            ((event._eventPreferencesBattery._plugged != null) &&
                                    (!event._eventPreferencesBattery._plugged.isEmpty()))){
                        if (event._eventPreferencesBattery._charging == 1)
                            batteryPassed = batteryPassed && isCharging;
                        else
                        if (event._eventPreferencesBattery._charging == 2)
                            batteryPassed = batteryPassed && (!isCharging);
                        //PPApplication.logE("[BAT] EventsHandler.doHandleEvents", "event._eventPreferencesBattery._plugged=" + event._eventPreferencesBattery._plugged);
                        if ((event._eventPreferencesBattery._plugged != null) &&
                                (!event._eventPreferencesBattery._plugged.isEmpty())) {
                            String[] splits = event._eventPreferencesBattery._plugged.split("\\|");
                            //PPApplication.logE("[BAT] EventsHandler.doHandleEvents", "splits.length=" + splits.length);
                            if (splits.length > 0) {
                                boolean passed = false;
                                for (String split : splits) {
                                    try {
                                        int plug = Integer.parseInt(split);
                                        //PPApplication.logE("[BAT] EventsHandler.doHandleEvents", "plug=" + plug);
                                        if ((plug == 1) && (plugged == BatteryManager.BATTERY_PLUGGED_AC)) {
                                            passed = true;
                                            break;
                                        }
                                        if ((plug == 2) && (plugged == BatteryManager.BATTERY_PLUGGED_USB)) {
                                            passed = true;
                                            break;
                                        }
                                        if ((plug == 3) && (plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS)) {
                                            passed = true;
                                            break;
                                        }
                                    } catch (Exception ignored) {
                                    }
                                }
                                batteryPassed = batteryPassed && passed;
                            }
                        }
                    } else if (event._eventPreferencesBattery._powerSaveMode)
                        batteryPassed = batteryPassed && isPowerSaveMode;
                } else
                    notAllowedBattery = true;

                /*if (PPApplication.logEnabled()) {
                    PPApplication.logE("[BAT] EventsHandler.doHandleEvents", "notAllowedBattery=" + notAllowedBattery);
                    PPApplication.logE("[BAT] EventsHandler.doHandleEvents", "batteryPassed=" + batteryPassed);
                }*/

                if (!notAllowedBattery) {
                    if (batteryPassed)
                        event._eventPreferencesBattery.setSensorPassed(EventPreferences.SENSOR_PASSED_PASSED);
                    else
                        event._eventPreferencesBattery.setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);
                }
            } else
                notAllowedBattery = true;
            int newSensorPassed = event._eventPreferencesBattery.getSensorPassed() & (~EventPreferences.SENSOR_PASSED_WAITING);
            if (oldSensorPassed != newSensorPassed) {
                event._eventPreferencesBattery.setSensorPassed(newSensorPassed);
                DatabaseHandler.getInstance(context).updateEventSensorPassed(event, DatabaseHandler.ETYPE_BATTERY);
            }
        }

        if (event._eventPreferencesCall._enabled) {
            int oldSensorPassed = event._eventPreferencesCall.getSensorPassed();
            if ((Event.isEventPreferenceAllowed(EventPreferencesCall.PREF_EVENT_CALL_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED)
                // permissions are checked in EditorProfilesActivity.displayRedTextToPreferencesNotification()
                /*&& Permissions.checkEventCallContacts(context, event, null)*//* &&
                  this is not required, is only for simulating ringing -> Permissions.checkEventPhoneBroadcast(context, event, null)*/) {
                int callEventType = ApplicationPreferences.prefEventCallEventType;
                String phoneNumber = ApplicationPreferences.prefEventCallPhoneNumber;

                /*if (PPApplication.logEnabled()) {
                    PPApplication.logE("[CALL] EventsHandler.doHandleEvents", "callEventType=" + callEventType);
                    PPApplication.logE("[CALL] EventsHandler.doHandleEvents", "phoneNumber=" + phoneNumber);
                }*/

                boolean phoneNumberFound = false;

                if (callEventType != EventPreferencesCall.PHONE_CALL_EVENT_UNDEFINED) {
                    if (callEventType == EventPreferencesCall.PHONE_CALL_EVENT_SERVICE_UNBIND)
                        callPassed = false;
                    else
                        phoneNumberFound = event._eventPreferencesCall.isPhoneNumberConfigured(phoneNumber/*, this*/);

                    //PPApplication.logE("[CALL] EventsHandler.doHandleEvents", "phoneNumberFound=" + phoneNumberFound);

                    if (phoneNumberFound) {
                        if (event._eventPreferencesCall._callEvent == EventPreferencesCall.CALL_EVENT_RINGING) {
                            //noinspection StatementWithEmptyBody
                            if ((callEventType == EventPreferencesCall.PHONE_CALL_EVENT_INCOMING_CALL_RINGING) ||
                                    ((callEventType == EventPreferencesCall.PHONE_CALL_EVENT_INCOMING_CALL_ANSWERED)))
                                ;//eventStart = eventStart && true;
                            else
                                callPassed = false;
                        } else if (event._eventPreferencesCall._callEvent == EventPreferencesCall.CALL_EVENT_INCOMING_CALL_ANSWERED) {
                            //noinspection StatementWithEmptyBody
                            if (callEventType == EventPreferencesCall.PHONE_CALL_EVENT_INCOMING_CALL_ANSWERED)
                                ;//eventStart = eventStart && true;
                            else
                                callPassed = false;
                        } else if (event._eventPreferencesCall._callEvent == EventPreferencesCall.CALL_EVENT_OUTGOING_CALL_STARTED) {
                            //noinspection StatementWithEmptyBody
                            if (callEventType == EventPreferencesCall.PHONE_CALL_EVENT_OUTGOING_CALL_ANSWERED)
                                ;//eventStart = eventStart && true;
                            else
                                callPassed = false;
                        } else
                        if ((event._eventPreferencesCall._callEvent == EventPreferencesCall.CALL_EVENT_MISSED_CALL) ||
                                (event._eventPreferencesCall._callEvent == EventPreferencesCall.CALL_EVENT_INCOMING_CALL_ENDED) ||
                                (event._eventPreferencesCall._callEvent == EventPreferencesCall.CALL_EVENT_OUTGOING_CALL_ENDED)) {
                            if (event._eventPreferencesCall._startTime > 0) {
                                int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();
                                long startTime = event._eventPreferencesCall._startTime - gmtOffset;

                                /*if (PPApplication.logEnabled()) {
                                    SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                                    String alarmTimeS = sdf.format(startTime);
                                    PPApplication.logE("[CALL] EventsHandler.doHandleEvents", "startTime=" + alarmTimeS);
                                }*/

                                // compute end datetime
                                long endAlarmTime = event._eventPreferencesCall.computeAlarm();
                                /*if (PPApplication.logEnabled()) {
                                    SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                                    String alarmTimeS = sdf.format(endAlarmTime);
                                    PPApplication.logE("[CALL] EventsHandler.doHandleEvents", "endAlarmTime=" + alarmTimeS);
                                }*/

                                Calendar now = Calendar.getInstance();
                                long nowAlarmTime = now.getTimeInMillis();
                                /*if (PPApplication.logEnabled()) {
                                    SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                                    String alarmTimeS = sdf.format(nowAlarmTime);
                                    PPApplication.logE("[CALL] EventsHandler.doHandleEvents", "nowAlarmTime=" + alarmTimeS);
                                }*/

                                if (sensorType.equals(EventsHandler.SENSOR_TYPE_PHONE_CALL)) {
                                    //noinspection StatementWithEmptyBody
                                    if (((callEventType == EventPreferencesCall.PHONE_CALL_EVENT_MISSED_CALL) && (event._eventPreferencesCall._callEvent == EventPreferencesCall.CALL_EVENT_MISSED_CALL)) ||
                                            ((callEventType == EventPreferencesCall.PHONE_CALL_EVENT_INCOMING_CALL_ENDED) && (event._eventPreferencesCall._callEvent == EventPreferencesCall.CALL_EVENT_INCOMING_CALL_ENDED)) ||
                                            ((callEventType == EventPreferencesCall.PHONE_CALL_EVENT_OUTGOING_CALL_ENDED) && (event._eventPreferencesCall._callEvent == EventPreferencesCall.CALL_EVENT_OUTGOING_CALL_ENDED)))
                                        ;//eventStart = eventStart && true;
                                    else
                                        callPassed = false;
                                } else if (!event._eventPreferencesCall._permanentRun) {
                                    if (sensorType.equals(EventsHandler.SENSOR_TYPE_PHONE_CALL_EVENT_END))
                                        callPassed = false;
                                    else
                                        callPassed = ((nowAlarmTime >= startTime) && (nowAlarmTime < endAlarmTime));
                                } else {
                                    callPassed = nowAlarmTime >= startTime;
                                }
                            } else
                                callPassed = false;
                        }

                        //if ((callEventType == PhoneCallBroadcastReceiver.CALL_EVENT_INCOMING_CALL_ENDED) ||
                        //        (callEventType == PhoneCallBroadcastReceiver.CALL_EVENT_OUTGOING_CALL_ENDED)) {
                        //    //callPassed = true;
                        //    //eventStart = eventStart && false;
                        //    callPassed = false;
                        //}
                    } else
                        callPassed = false;

                    //PPApplication.logE("[CALL] EventsHandler.doHandleEvents", "callPassed=" + callPassed);

                    if (!callPassed) {
                        //PPApplication.logE("[CALL] EventsHandler.doHandleEvents", "startTime=0");
                        event._eventPreferencesCall._startTime = 0;
                        DatabaseHandler.getInstance(context).updateCallStartTime(event);
                    }
                } else {
                    if ((event._eventPreferencesCall._callEvent == EventPreferencesCall.CALL_EVENT_MISSED_CALL) ||
                            (event._eventPreferencesCall._callEvent == EventPreferencesCall.CALL_EVENT_INCOMING_CALL_ENDED) ||
                            (event._eventPreferencesCall._callEvent == EventPreferencesCall.CALL_EVENT_OUTGOING_CALL_ENDED)) {
                        if (event._eventPreferencesCall._startTime > 0) {
                            int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();
                            long startTime = event._eventPreferencesCall._startTime - gmtOffset;

                            /*if (PPApplication.logEnabled()) {
                                SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                                String alarmTimeS = sdf.format(startTime);
                                PPApplication.logE("[CALL] EventsHandler.doHandleEvents", "startTime=" + alarmTimeS);
                            }*/

                            // compute end datetime
                            long endAlarmTime = event._eventPreferencesCall.computeAlarm();
                            /*if (PPApplication.logEnabled()) {
                                SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                                String alarmTimeS = sdf.format(endAlarmTime);
                                PPApplication.logE("[CALL] EventsHandler.doHandleEvents", "endAlarmTime=" + alarmTimeS);
                            }*/

                            Calendar now = Calendar.getInstance();
                            long nowAlarmTime = now.getTimeInMillis();
                            /*if (PPApplication.logEnabled()) {
                                SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                                String alarmTimeS = sdf.format(nowAlarmTime);
                                PPApplication.logE("[CALL] EventsHandler.doHandleEvents", "nowAlarmTime=" + alarmTimeS);
                            }*/

                            if (!event._eventPreferencesCall._permanentRun) {
                                if (sensorType.equals(EventsHandler.SENSOR_TYPE_PHONE_CALL_EVENT_END))
                                    callPassed = false;
                                else
                                    callPassed = ((nowAlarmTime >= startTime) && (nowAlarmTime < endAlarmTime));
                            } else {
                                callPassed = nowAlarmTime >= startTime;
                            }
                        }
                        else
                            callPassed = false;

                        if (!callPassed) {
                            //PPApplication.logE("[CALL] EventsHandler.doHandleEvents", "startTime=0");
                            event._eventPreferencesCall._startTime = 0;
                            DatabaseHandler.getInstance(context).updateCallStartTime(event);
                        }
                    }
                    else
                        notAllowedCall = true;
                }

                if (!notAllowedCall) {
                    if (callPassed)
                        event._eventPreferencesCall.setSensorPassed(EventPreferences.SENSOR_PASSED_PASSED);
                    else
                        event._eventPreferencesCall.setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);
                }
            }
            else
                notAllowedCall = true;
            int newSensorPassed = event._eventPreferencesCall.getSensorPassed() & (~EventPreferences.SENSOR_PASSED_WAITING);
            if (oldSensorPassed != newSensorPassed) {
                event._eventPreferencesCall.setSensorPassed(newSensorPassed);
                DatabaseHandler.getInstance(context).updateEventSensorPassed(event, DatabaseHandler.ETYPE_CALL);
            }
        }

        if (event._eventPreferencesPeripherals._enabled) {
            int oldSensorPassed = event._eventPreferencesPeripherals.getSensorPassed();
            if (Event.isEventPreferenceAllowed(EventPreferencesPeripherals.PREF_EVENT_PERIPHERAL_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                if ((event._eventPreferencesPeripherals._peripheralType == EventPreferencesPeripherals.PERIPHERAL_TYPE_DESK_DOCK) ||
                        (event._eventPreferencesPeripherals._peripheralType == EventPreferencesPeripherals.PERIPHERAL_TYPE_CAR_DOCK)) {
                    // get dock status
                    IntentFilter iFilter = new IntentFilter(Intent.ACTION_DOCK_EVENT);
                    Intent dockStatus = context.registerReceiver(null, iFilter);

                    if (dockStatus != null) {
                        int dockState = dockStatus.getIntExtra(Intent.EXTRA_DOCK_STATE, -1);
                        boolean isDocked = dockState != Intent.EXTRA_DOCK_STATE_UNDOCKED;
                        boolean isCar = dockState == Intent.EXTRA_DOCK_STATE_CAR;
                        boolean isDesk = dockState == Intent.EXTRA_DOCK_STATE_DESK ||
                                dockState == Intent.EXTRA_DOCK_STATE_LE_DESK ||
                                dockState == Intent.EXTRA_DOCK_STATE_HE_DESK;

                        if (isDocked) {
                            if ((event._eventPreferencesPeripherals._peripheralType == EventPreferencesPeripherals.PERIPHERAL_TYPE_DESK_DOCK)
                                    && isDesk)
                                peripheralPassed = true;
                            else
                                peripheralPassed = (event._eventPreferencesPeripherals._peripheralType == EventPreferencesPeripherals.PERIPHERAL_TYPE_CAR_DOCK)
                                        && isCar;
                        } else
                            peripheralPassed = false;
                        //eventStart = eventStart && peripheralPassed;
                    } else
                        notAllowedPeripheral = true;
                } else if ((event._eventPreferencesPeripherals._peripheralType == EventPreferencesPeripherals.PERIPHERAL_TYPE_WIRED_HEADSET) ||
                        (event._eventPreferencesPeripherals._peripheralType == EventPreferencesPeripherals.PERIPHERAL_TYPE_BLUETOOTH_HEADSET) ||
                        (event._eventPreferencesPeripherals._peripheralType == EventPreferencesPeripherals.PERIPHERAL_TYPE_HEADPHONES)) {
                    boolean wiredHeadsetConnected = ApplicationPreferences.prefWiredHeadsetConnected;
                    boolean wiredHeadsetMicrophone = ApplicationPreferences.prefWiredHeadsetMicrophone;
                    boolean bluetoothHeadsetConnected = ApplicationPreferences.prefBluetoothHeadsetConnected;
                    boolean bluetoothHeadsetMicrophone = ApplicationPreferences.prefBluetoothHeadsetMicrophone;

                    peripheralPassed = false;
                    if (wiredHeadsetConnected) {
                        if ((event._eventPreferencesPeripherals._peripheralType == EventPreferencesPeripherals.PERIPHERAL_TYPE_WIRED_HEADSET)
                                && wiredHeadsetMicrophone)
                            peripheralPassed = true;
                        else
                        if ((event._eventPreferencesPeripherals._peripheralType == EventPreferencesPeripherals.PERIPHERAL_TYPE_HEADPHONES)
                                && (!wiredHeadsetMicrophone))
                            peripheralPassed = true;
                    }
                    if (bluetoothHeadsetConnected) {
                        if ((event._eventPreferencesPeripherals._peripheralType == EventPreferencesPeripherals.PERIPHERAL_TYPE_BLUETOOTH_HEADSET)
                                && bluetoothHeadsetMicrophone)
                            peripheralPassed = true;
                    }
                    //eventStart = eventStart && peripheralPassed;
                }

                if (!notAllowedPeripheral) {
                    if (peripheralPassed)
                        event._eventPreferencesPeripherals.setSensorPassed(EventPreferences.SENSOR_PASSED_PASSED);
                    else
                        event._eventPreferencesPeripherals.setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);
                }
            } else
                notAllowedPeripheral = true;
            int newSensorPassed = event._eventPreferencesPeripherals.getSensorPassed() & (~EventPreferences.SENSOR_PASSED_WAITING);
            if (oldSensorPassed != newSensorPassed) {
                event._eventPreferencesPeripherals.setSensorPassed(newSensorPassed);
                DatabaseHandler.getInstance(context).updateEventSensorPassed(event, DatabaseHandler.ETYPE_PERIPHERAL);
            }
        }

        if (event._eventPreferencesCalendar._enabled) {
            int oldSensorPassed = event._eventPreferencesCalendar.getSensorPassed();
            if ((Event.isEventPreferenceAllowed(EventPreferencesCalendar.PREF_EVENT_CALENDAR_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED)
                // permissions are checked in EditorProfilesActivity.displayRedTextToPreferencesNotification()
                /*&& (Permissions.checkEventCalendar(context, event, null))*/) {
                // compute start datetime
                long startAlarmTime;
                long endAlarmTime;

                if (event._eventPreferencesCalendar._eventFound) {
                    startAlarmTime = event._eventPreferencesCalendar.computeAlarm(true);

                    //String alarmTimeS = DateFormat.getDateFormat(context).format(startAlarmTime) +
                    //        " " + DateFormat.getTimeFormat(context).format(startAlarmTime);
                    //PPApplication.logE("EventsHandler.doHandleEvents", "startAlarmTime=" + alarmTimeS);

                    endAlarmTime = event._eventPreferencesCalendar.computeAlarm(false);

                    //alarmTimeS = DateFormat.getDateFormat(context).format(endAlarmTime) +
                    //        " " + DateFormat.getTimeFormat(context).format(endAlarmTime);
                    //PPApplication.logE("EventsHandler.doHandleEvents", "endAlarmTime=" + alarmTimeS);

                    Calendar now = Calendar.getInstance();
                    long nowAlarmTime = now.getTimeInMillis();
                    //alarmTimeS = DateFormat.getDateFormat(context).format(nowAlarmTime) +
                    //        " " + DateFormat.getTimeFormat(context).format(nowAlarmTime);
                    //PPApplication.logE("EventsHandler.doHandleEvents", "nowAlarmTime=" + alarmTimeS);

                    calendarPassed = ((nowAlarmTime >= startAlarmTime) && (nowAlarmTime < endAlarmTime));
                } else
                    calendarPassed = false;

                if (!notAllowedCalendar) {
                    if (calendarPassed)
                        event._eventPreferencesCalendar.setSensorPassed(EventPreferences.SENSOR_PASSED_PASSED);
                    else
                        event._eventPreferencesCalendar.setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);
                }
            } else
                notAllowedCalendar = true;
            int newSensorPassed = event._eventPreferencesCalendar.getSensorPassed() & (~EventPreferences.SENSOR_PASSED_WAITING);
            if (oldSensorPassed != newSensorPassed) {
                event._eventPreferencesCalendar.setSensorPassed(newSensorPassed);
                DatabaseHandler.getInstance(context).updateEventSensorPassed(event, DatabaseHandler.ETYPE_CALENDAR);
            }
        }


        if (event._eventPreferencesWifi._enabled) {
            int oldSensorPassed = event._eventPreferencesWifi.getSensorPassed();
            if ((Event.isEventPreferenceAllowed(EventPreferencesWifi.PREF_EVENT_WIFI_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED)
                // permissions are checked in EditorProfilesActivity.displayRedTextToPreferencesNotification()
                /*&& Permissions.checkEventLocation(context, event, null)*/) {
                //if (event._name.equals("Doma"))
                //    PPApplication.logE("[WiFi] EventsHandler.doHandleEvents", "-------- eventSSID=" + event._eventPreferencesWifi._SSID);

                wifiPassed = false;

                WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                boolean isWifiEnabled = wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED;

                List<WifiSSIDData> wifiConfigurationList = WifiScanWorker.getWifiConfigurationList(context);

                boolean done = false;

                if (isWifiEnabled) {
                    //if (event._name.equals("Doma"))
                    //    PPApplication.logE("[WiFi] EventsHandler.doHandleEvents", "wifiStateEnabled=true");

                    //PPApplication.logE("----- EventsHandler.doHandleEvents","-- eventSSID="+event._eventPreferencesWifi._SSID);

                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();

                    boolean wifiConnected = false;

                    ConnectivityManager connManager = null;
                    try {
                        connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                    } catch (Exception ignored) {
                        // java.lang.NullPointerException: missing IConnectivityManager
                        // Dual SIM?? Bug in Android ???
                    }
                    if (connManager != null) {
                        //if (android.os.Build.VERSION.SDK_INT >= 21) {
                        Network[] networks = connManager.getAllNetworks();
                        if ((networks != null) && (networks.length > 0)) {
                            for (Network network : networks) {
                                try {
                                    if (Build.VERSION.SDK_INT < 28) {
                                        NetworkInfo ntkInfo = connManager.getNetworkInfo(network);
                                        if (ntkInfo != null) {
                                            if (ntkInfo.getType() == ConnectivityManager.TYPE_WIFI && ntkInfo.isConnected()) {
                                                if (wifiInfo != null) {
                                                    wifiConnected = true;
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                    else {
                                        NetworkInfo networkInfo = connManager.getNetworkInfo(network);
                                        if((networkInfo != null) && networkInfo.isConnected()) {
                                            NetworkCapabilities networkCapabilities = connManager.getNetworkCapabilities(network);
                                            if ((networkCapabilities != null) && networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                                                wifiConnected = true;
                                                break;
                                            }
                                        }
                                    }
                                } catch (Exception ignored) {
                                }
                            }
                        }
                        /*} else {
                            //noinspection deprecation
                            NetworkInfo ntkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                            wifiConnected = (ntkInfo != null) && ntkInfo.isConnected();
                        }*/
                    }

                    if (wifiConnected) {
                        /*if (PPApplication.logEnabled()) {
                            if (event._name.equals("Doma")) {
                                PPApplication.logE("[WiFi] EventsHandler.doHandleEvents", "wifi connected");
                                PPApplication.logE("[WiFi] EventsHandler.doHandleEvents", "wifiSSID=" + WifiScanWorker.getSSID(wifiManager, wifiInfo, wifiConfigurationList));
                                PPApplication.logE("[WiFi] EventsHandler.doHandleEvents", "wifiBSSID=" + wifiInfo.getBSSID());
                            }
                        }*/

                        //PPApplication.logE("----- EventsHandler.doHandleEvents","SSID="+event._eventPreferencesWifi._SSID);

                        String[] splits = event._eventPreferencesWifi._SSID.split("\\|");
                        boolean[] connected = new boolean[splits.length];

                        int i = 0;
                        for (String _ssid : splits) {
                            connected[i] = false;
                            switch (_ssid) {
                                case EventPreferencesWifi.ALL_SSIDS_VALUE:
                                    connected[i] = true;
                                    break;
                                case EventPreferencesWifi.CONFIGURED_SSIDS_VALUE:
                                    for (WifiSSIDData data : wifiConfigurationList) {
                                        connected[i] = WifiScanWorker.compareSSID(wifiManager, wifiInfo, data.ssid.replace("\"", ""), wifiConfigurationList);
                                        if (connected[i])
                                            break;
                                    }
                                    break;
                                default:
                                    connected[i] = WifiScanWorker.compareSSID(wifiManager, wifiInfo, _ssid, wifiConfigurationList);
                                    break;
                            }
                            i++;
                        }

                        if (event._eventPreferencesWifi._connectionType == EventPreferencesWifi.CTYPE_NOT_CONNECTED) {
                            wifiPassed = true;
                            for (boolean conn : connected) {
                                if (conn) {
                                    wifiPassed = false;
                                    break;
                                }
                            }
                            // not use scanner data
                            done = true;
                        }
                        else
                        if (event._eventPreferencesWifi._connectionType == EventPreferencesWifi.CTYPE_CONNECTED) {
                            wifiPassed = false;
                            for (boolean conn : connected) {
                                if (conn) {
                                    wifiPassed = true;
                                    break;
                                }
                            }
                            // not use scanner data
                            done = true;
                        }
                    } else {
                        //if (event._name.equals("Doma"))
                        //    PPApplication.logE("[WiFi] EventsHandler.doHandleEvents", "wifi not connected");

                        if ((event._eventPreferencesWifi._connectionType == EventPreferencesWifi.CTYPE_CONNECTED) ||
                                (event._eventPreferencesWifi._connectionType == EventPreferencesWifi.CTYPE_NOT_CONNECTED)) {
                            // not use scanner data
                            done = true;
                            wifiPassed = (event._eventPreferencesWifi._connectionType == EventPreferencesWifi.CTYPE_NOT_CONNECTED);
                        }
                    }
                } else {
                    //if (event._name.equals("Doma"))
                    //    PPApplication.logE("[WiFi] EventsHandler.doHandleEvents", "wifiStateEnabled=false");
                    if ((event._eventPreferencesWifi._connectionType == EventPreferencesWifi.CTYPE_CONNECTED) ||
                            (event._eventPreferencesWifi._connectionType == EventPreferencesWifi.CTYPE_NOT_CONNECTED)) {
                        // not use scanner data
                        done = true;
                        wifiPassed = (event._eventPreferencesWifi._connectionType == EventPreferencesWifi.CTYPE_NOT_CONNECTED);
                    }
                }

                // if (event._name.equals("Doma"))
                //     PPApplication.logE("[WiFi] EventsHandler.doHandleEvents", "wifiPassed - connected =" + wifiPassed);

                if ((event._eventPreferencesWifi._connectionType == EventPreferencesWifi.CTYPE_NEARBY) ||
                        (event._eventPreferencesWifi._connectionType == EventPreferencesWifi.CTYPE_NOT_NEARBY)) {
                    if (!done) {
                        if (!ApplicationPreferences.applicationEventWifiEnableScanning) {
                            //if (forRestartEvents)
                            //    wifiPassed = (EventPreferences.SENSOR_PASSED_PASSED & event._eventPreferencesWifi.getSensorPassed()) == EventPreferences.SENSOR_PASSED_PASSED;
                            //else
                            // not allowed for disabled scanning
                            //    notAllowedWifi = true;
                            wifiPassed = false;
                        } else {
                            //PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                            if (!PPApplication.isScreenOn && ApplicationPreferences.applicationEventWifiScanOnlyWhenScreenIsOn) {
                                if (forRestartEvents)
                                    wifiPassed = (EventPreferences.SENSOR_PASSED_PASSED & event._eventPreferencesWifi.getSensorPassed()) == EventPreferences.SENSOR_PASSED_PASSED;
                                else
                                    // not allowed for screen Off
                                    notAllowedWifi = true;
                            } else {

                                wifiPassed = false;

                                List<WifiSSIDData> scanResults = WifiScanWorker.getScanResults(context);

                                //PPApplication.logE("----- EventsHandler.doHandleEvents","scanResults="+scanResults);

                                if (scanResults != null) {
                                    /*if (PPApplication.logEnabled()) {
                                        if (event._name.equals("Doma")) {
                                            PPApplication.logE("[WiFi] EventsHandler.doHandleEvents", "scanResults != null");
                                            PPApplication.logE("[WiFi] EventsHandler.doHandleEvents", "scanResults.size=" + scanResults.size());
                                            //PPApplication.logE("----- EventsHandler.doHandleEvents","-- eventSSID="+event._eventPreferencesWifi._SSID);
                                        }
                                    }*/

                                    for (WifiSSIDData result : scanResults) {
                                        /*if (PPApplication.logEnabled()) {
                                            if (event._name.equals("Doma")) {
                                                PPApplication.logE("[WiFi] EventsHandler.doHandleEvents", "scanSSID=" + result.ssid);
                                                PPApplication.logE("[WiFi] EventsHandler.doHandleEvents", "scanBSSID=" + result.bssid);
                                            }
                                        }*/
                                        String[] splits = event._eventPreferencesWifi._SSID.split("\\|");
                                        boolean[] nearby = new boolean[splits.length];
                                        int i = 0;
                                        for (String _ssid : splits) {
                                            nearby[i] = false;
                                            switch (_ssid) {
                                                case EventPreferencesWifi.ALL_SSIDS_VALUE:
                                                    //if (event._name.equals("Doma"))
                                                    //    PPApplication.logE("[WiFi] EventsHandler.doHandleEvents", "all ssids");
                                                    nearby[i] = true;
                                                    break;
                                                case EventPreferencesWifi.CONFIGURED_SSIDS_VALUE:
                                                    //if (event._name.equals("Doma"))
                                                    //    PPApplication.logE("[WiFi] EventsHandler.doHandleEvents", "configured ssids");
                                                    for (WifiSSIDData data : wifiConfigurationList) {
                                                        if (WifiScanWorker.compareSSID(result, data.ssid.replace("\"", ""), wifiConfigurationList)) {
                                                            /*if (PPApplication.logEnabled()) {
                                                                if (event._name.equals("Doma")) {
                                                                    PPApplication.logE("[WiFi] EventsHandler.doHandleEvents", "configured SSID=" + data.ssid.replace("\"", ""));
                                                                    PPApplication.logE("[WiFi] EventsHandler.doHandleEvents", "wifi found");
                                                                }
                                                            }*/
                                                            nearby[i] = true;
                                                            break;
                                                        }
                                                    }
                                                    break;
                                                default:
                                                    if (WifiScanWorker.compareSSID(result, _ssid, wifiConfigurationList)) {
                                                        /*if (PPApplication.logEnabled()) {
                                                            if (event._name.equals("Doma")) {
                                                                PPApplication.logE("[WiFi] EventsHandler.doHandleEvents", "event SSID=" + event._eventPreferencesWifi._SSID);
                                                                PPApplication.logE("[WiFi] EventsHandler.doHandleEvents", "wifi found");
                                                            }
                                                        }*/
                                                        nearby[i] = true;
                                                    }
                                                    break;
                                            }
                                            i++;
                                        }

                                        done = false;
                                        if (event._eventPreferencesWifi._connectionType == EventPreferencesWifi.CTYPE_NOT_NEARBY) {
                                            wifiPassed = true;
                                            for (boolean inF : nearby) {
                                                if (inF) {
                                                    done = true;
                                                    wifiPassed = false;
                                                    break;
                                                }
                                            }
                                        }
                                        else {
                                            wifiPassed = false;
                                            for (boolean inF : nearby) {
                                                if (inF) {
                                                    done = true;
                                                    wifiPassed = true;
                                                    break;
                                                }
                                            }
                                        }
                                        if (done)
                                            break;
                                    }
                                    //if (event._name.equals("Doma"))
                                    //    PPApplication.logE("[WiFi] EventsHandler.doHandleEvents", "wifiPassed - in front =" + wifiPassed);

                                    if (!done) {
                                        if (scanResults.size() == 0) {
                                            //if (event._name.equals("Doma"))
                                            //    PPApplication.logE("[WiFi] EventsHandler.doHandleEvents", "scanResult is empty");

                                            if (event._eventPreferencesWifi._connectionType == EventPreferencesWifi.CTYPE_NOT_NEARBY)
                                                wifiPassed = true;

                                            //if (event._name.equals("Doma"))
                                            //    PPApplication.logE("[WiFi] EventsHandler.doHandleEvents", "wifiPassed - in front - for empty scanResult =" + wifiPassed);
                                        }
                                    }

                                } /*else
                                    if (event._name.equals("Doma"))
                                        PPApplication.logE("[WiFi] EventsHandler.doHandleEvents", "scanResults == null");*/
                            }
                        }
                    }
                }

                /*if (event._name.equals("Doma")) {
                    PPApplication.logE("[WiFi] EventsHandler.doHandleEvents", "------- wifiPassed=" + wifiPassed);
                    PPApplication.logE("[WiFi] EventsHandler.doHandleEvents", "------- notAllowedWifi=" + notAllowedWifi);
                }*/

                if (!notAllowedWifi) {
                    if (wifiPassed)
                        event._eventPreferencesWifi.setSensorPassed(EventPreferences.SENSOR_PASSED_PASSED);
                    else
                        event._eventPreferencesWifi.setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);
                }
            } else
                notAllowedWifi = true;
            int newSensorPassed = event._eventPreferencesWifi.getSensorPassed() & (~EventPreferences.SENSOR_PASSED_WAITING);
            if (oldSensorPassed != newSensorPassed) {
                event._eventPreferencesWifi.setSensorPassed(newSensorPassed);
                DatabaseHandler.getInstance(context).updateEventSensorPassed(event, DatabaseHandler.ETYPE_WIFI);
            }
        }

        if (event._eventPreferencesScreen._enabled) {
            int oldSensorPassed = event._eventPreferencesScreen.getSensorPassed();
            if ((Event.isEventPreferenceAllowed(EventPreferencesScreen.PREF_EVENT_SCREEN_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED)) {
                //PPApplication.logE("[Screen] EventsHandler.doHandleEvents", "xxx");

                //boolean isScreenOn;
                //PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                boolean keyguardShowing = false;

                if (event._eventPreferencesScreen._whenUnlocked) {
                    KeyguardManager kgMgr = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
                    keyguardShowing = kgMgr.isKeyguardLocked();
                }
                /*if (PPApplication.logEnabled()) {
                    PPApplication.logE("[Screen] EventsHandler.doHandleEvents", "PPApplication.isScreenOn=" + PPApplication.isScreenOn);
                    PPApplication.logE("[Screen] EventsHandler.doHandleEvents", "keyguardShowing=" + keyguardShowing);
                }*/

                if (event._eventPreferencesScreen._eventType == EventPreferencesScreen.ETYPE_SCREENON) {
                    // event type = screen is on
                    if (event._eventPreferencesScreen._whenUnlocked)
                        // passed if screen is on and unlocked
                        if (PPApplication.isScreenOn) {
                            screenPassed = !keyguardShowing;
                        }
                        else
                        if (!PPApplication.isScreenOn) {
                            screenPassed = !keyguardShowing;
                        }
                        //screenPassed = PPApplication.isScreenOn && (!keyguardShowing);
                        else
                            screenPassed = PPApplication.isScreenOn;
                } else {
                    // event type = screen is off
                    if (event._eventPreferencesScreen._whenUnlocked) {
                        // passed if screen is off and locked
                        if (!PPApplication.isScreenOn) {
                            screenPassed = keyguardShowing;
                        }
                        else
                        if (PPApplication.isScreenOn) {
                            screenPassed = keyguardShowing;
                        }
                        //screenPassed = (!PPApplication.isScreenOn) && keyguardShowing;
                    }
                    else
                        screenPassed = !PPApplication.isScreenOn;
                }

                //PPApplication.logE("[Screen] EventsHandler.doHandleEvents", "screenPassed="+screenPassed);

                if (!notAllowedScreen) {
                    if (screenPassed)
                        event._eventPreferencesScreen.setSensorPassed(EventPreferences.SENSOR_PASSED_PASSED);
                    else
                        event._eventPreferencesScreen.setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);
                }
            } else
                notAllowedScreen = true;
            int newSensorPassed = event._eventPreferencesScreen.getSensorPassed() & (~EventPreferences.SENSOR_PASSED_WAITING);
            if (oldSensorPassed != newSensorPassed) {
                event._eventPreferencesScreen.setSensorPassed(newSensorPassed);
                DatabaseHandler.getInstance(context).updateEventSensorPassed(event, DatabaseHandler.ETYPE_SCREEN);
            }
        }


        if (event._eventPreferencesBluetooth._enabled) {
            int oldSensorPassed = event._eventPreferencesBluetooth.getSensorPassed();
            if ((Event.isEventPreferenceAllowed(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED)
                // permissions are checked in EditorProfilesActivity.displayRedTextToPreferencesNotification()
                /*&& Permissions.checkEventLocation(context, event, null)
                && Permissions.checkEventBluetoothForEMUI(context, event, null)*/) {
                bluetoothPassed = false;

                List<BluetoothDeviceData> boundedDevicesList = BluetoothScanWorker.getBoundedDevicesList(context);

                boolean done = false;

                BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter(); //BluetoothScanWorker.getBluetoothAdapter(context);
                if (bluetooth != null) {
                    boolean isBluetoothEnabled = bluetooth.isEnabled();

                    if (isBluetoothEnabled) {
                        /*if (PPApplication.logEnabled()) {
                            PPApplication.logE("[BTScan] EventsHandler.doHandleEvents", "bluetoothEnabled=true");
                            PPApplication.logE("[BTScan] EventsHandler.doHandleEvents", "-- eventAdapterName=" + event._eventPreferencesBluetooth._adapterName);
                        }*/

                        //List<BluetoothDeviceData> connectedDevices = BluetoothConnectedDevices.getConnectedDevices(context);
                        BluetoothConnectionBroadcastReceiver.getConnectedDevices(context);

                        if (BluetoothConnectionBroadcastReceiver.isBluetoothConnected(null, "")) {
                            //if (BluetoothConnectedDevices.isBluetoothConnected(connectedDevices,null, "")) {

                            //PPApplication.logE("[BTScan] EventsHandler.doHandleEvents", "any device connected");

                            String[] splits = event._eventPreferencesBluetooth._adapterName.split("\\|");
                            boolean[] connected = new boolean[splits.length];

                            int i = 0;
                            for (String _bluetoothName : splits) {
                                connected[i] = false;
                                switch (_bluetoothName) {
                                    case EventPreferencesBluetooth.ALL_BLUETOOTH_NAMES_VALUE:
                                        //PPApplication.logE("[BTScan] EventsHandler.doHandleEvents", "any device connected");
                                        connected[i] = true;
                                        break;
                                    case EventPreferencesBluetooth.CONFIGURED_BLUETOOTH_NAMES_VALUE:
                                        for (BluetoothDeviceData data : boundedDevicesList) {
                                            /*if (PPApplication.logEnabled()) {
                                                PPApplication.logE("[BTScan] EventsHandler.doHandleEvents", "boundedDevice.name=" + data.getName());
                                                PPApplication.logE("[BTScan] EventsHandler.doHandleEvents", "boundedDevice.address=" + data.getAddress());
                                            }*/
                                            connected[i] = BluetoothConnectionBroadcastReceiver.isBluetoothConnected(data, "");
                                            if (connected[i])
                                                break;
                                        }
                                        //PPApplication.logE("[BTScan] EventsHandler.doHandleEvents", "paired device connected=" + connected[i]);
                                        break;
                                    default:
                                        connected[i] = BluetoothConnectionBroadcastReceiver.isBluetoothConnected(null, _bluetoothName);
                                        //PPApplication.logE("[BTScan] EventsHandler.doHandleEvents", "event sensor device connected=" + connected[i]);
                                        break;
                                }
                                i++;
                            }

                            if (event._eventPreferencesBluetooth._connectionType == EventPreferencesBluetooth.CTYPE_NOT_CONNECTED) {
                                bluetoothPassed = true;
                                for (boolean conn : connected) {
                                    if (conn) {
                                        bluetoothPassed = false;
                                        break;
                                    }
                                }
                                // not use scanner data
                                done = true;
                            }
                            else
                            if (event._eventPreferencesBluetooth._connectionType == EventPreferencesBluetooth.CTYPE_CONNECTED) {
                                bluetoothPassed = false;
                                for (boolean conn : connected) {
                                    if (conn) {
                                        // when is connected to configured bt name, is also nearby
                                        bluetoothPassed = true;
                                        break;
                                    }
                                }
                                // not use scanner data
                                done = true;
                            }
                        } else {
                            //PPApplication.logE("[BTScan] EventsHandler.doHandleEvents", "not any device connected");

                            if ((event._eventPreferencesBluetooth._connectionType == EventPreferencesBluetooth.CTYPE_CONNECTED) ||
                                    (event._eventPreferencesBluetooth._connectionType == EventPreferencesBluetooth.CTYPE_NOT_CONNECTED)) {
                                // not use scanner data
                                done = true;
                                bluetoothPassed = (event._eventPreferencesBluetooth._connectionType == EventPreferencesBluetooth.CTYPE_NOT_CONNECTED);
                            }
                        }
                    } else {
                        //PPApplication.logE("[BTScan] EventsHandler.doHandleEvents", "bluetoothEnabled=true");

                        if ((event._eventPreferencesBluetooth._connectionType == EventPreferencesBluetooth.CTYPE_CONNECTED) ||
                                (event._eventPreferencesBluetooth._connectionType == EventPreferencesBluetooth.CTYPE_NOT_CONNECTED)) {
                            // not use scanner data
                            done = true;
                            bluetoothPassed = (event._eventPreferencesBluetooth._connectionType == EventPreferencesBluetooth.CTYPE_NOT_CONNECTED);
                        }
                    }
                }

                //PPApplication.logE("[BTScan] EventsHandler.doHandleEvents", "bluetoothPassed=" + bluetoothPassed);

                if ((event._eventPreferencesBluetooth._connectionType == EventPreferencesBluetooth.CTYPE_NEARBY) ||
                        (event._eventPreferencesBluetooth._connectionType == EventPreferencesBluetooth.CTYPE_NOT_NEARBY)) {
                    if (!done) {
                        if (!ApplicationPreferences.applicationEventBluetoothEnableScanning) {
                            //if (forRestartEvents)
                            //    bluetoothPassed = (EventPreferences.SENSOR_PASSED_PASSED & event._eventPreferencesBluetooth.getSensorPassed()) == EventPreferences.SENSOR_PASSED_PASSED;
                            //else
                            // not allowed for disabled scanning
                            //    notAllowedBluetooth = true;
                            bluetoothPassed = false;
                        } else {
                            //PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                            if (!PPApplication.isScreenOn && ApplicationPreferences.applicationEventBluetoothScanOnlyWhenScreenIsOn) {
                                if (forRestartEvents)
                                    bluetoothPassed = (EventPreferences.SENSOR_PASSED_PASSED & event._eventPreferencesBluetooth.getSensorPassed()) == EventPreferences.SENSOR_PASSED_PASSED;
                                else
                                    // not allowed for screen Off
                                    notAllowedBluetooth = true;
                            } else {
                                bluetoothPassed = false;

                                List<BluetoothDeviceData> scanResults = BluetoothScanWorker.getScanResults(context);

                                if (scanResults != null) {
                                    //PPApplication.logE("[BTScan] EventsHandler.doHandleEvents", "scanResults.size="+scanResults.size());

                                    for (BluetoothDeviceData device : scanResults) {
                                        /*if (PPApplication.logEnabled()) {
                                            PPApplication.logE("[BTScan] EventsHandler.doHandleEvents", "device.getName=" + device.getName());
                                            PPApplication.logE("[BTScan] EventsHandler.doHandleEvents", "device.getAddress=" + device.getAddress());
                                        }*/
                                        String[] splits = event._eventPreferencesBluetooth._adapterName.split("\\|");
                                        boolean[] nearby = new boolean[splits.length];
                                        int i = 0;
                                        for (String _bluetoothName : splits) {
                                            nearby[i] = false;
                                            switch (_bluetoothName) {
                                                case EventPreferencesBluetooth.ALL_BLUETOOTH_NAMES_VALUE:
                                                    nearby[i] = true;
                                                    break;
                                                case EventPreferencesBluetooth.CONFIGURED_BLUETOOTH_NAMES_VALUE:
                                                    for (BluetoothDeviceData data : boundedDevicesList) {
                                                        String _device = device.getName().toUpperCase();
                                                        String _adapterName = data.getName().toUpperCase();
                                                        if (Wildcard.match(_device, _adapterName, '_', '%', true)) {
                                                            //PPApplication.logE("[BTScan] EventsHandler.doHandleEvents", "bluetooth found");
                                                            //PPApplication.logE("@@@ EventsHandler.doHandleEvents","bluetoothAdapterName="+device.getName());
                                                            //PPApplication.logE("@@@ EventsHandler.doHandleEvents","bluetoothAddress="+device.getAddress());
                                                            nearby[i] = true;
                                                            break;
                                                        }
                                                    }
                                                    break;
                                                default:
                                                    String _device = device.getName().toUpperCase();
                                                    if ((device.getName() == null) || device.getName().isEmpty()) {
                                                        // scanned device has not name (hidden BT?)
                                                        if ((device.getAddress() != null) && (!device.getAddress().isEmpty())) {
                                                            // device has address
                                                            for (BluetoothDeviceData data : boundedDevicesList) {
                                                                if ((data.getAddress() != null) && data.getAddress().equals(device.getAddress())) {
                                                                    //PPApplication.logE("[BTScan] EventsHandler.doHandleEvents", "bluetooth found");
                                                                    //PPApplication.logE("@@@ EventsHandler.doHandleEvents","bluetoothAdapterName="+device.getName());
                                                                    //PPApplication.logE("@@@ EventsHandler.doHandleEvents","bluetoothAddress="+device.getAddress());
                                                                    nearby[i] = true;
                                                                    break;
                                                                }
                                                            }
                                                        }
                                                    } else {
                                                        String _adapterName = _bluetoothName.toUpperCase();
                                                        if (Wildcard.match(_device, _adapterName, '_', '%', true)) {
                                                            //PPApplication.logE("[BTScan] EventsHandler.doHandleEvents", "bluetooth found");
                                                            //PPApplication.logE("@@@ EventsHandler.doHandleEvents","bluetoothAdapterName="+device.getName());
                                                            //PPApplication.logE("@@@ EventsHandler.doHandleEvents","bluetoothAddress="+device.getAddress());
                                                            nearby[i] = true;
                                                        }
                                                    }
                                                    break;
                                            }
                                            i++;
                                        }

                                        done = false;
                                        if (event._eventPreferencesBluetooth._connectionType == EventPreferencesBluetooth.CTYPE_NOT_NEARBY) {
                                            bluetoothPassed = true;
                                            for (boolean inF : nearby) {
                                                if (inF) {
                                                    done = true;
                                                    bluetoothPassed = false;
                                                    break;
                                                }
                                            }
                                        }
                                        else {
                                            bluetoothPassed = false;
                                            for (boolean inF : nearby) {
                                                if (inF) {
                                                    done = true;
                                                    bluetoothPassed = true;
                                                    break;
                                                }
                                            }
                                        }
                                        if (done)
                                            break;
                                    }
                                    //PPApplication.logE("[BTScan] EventsHandler.doHandleEvents", "bluetoothPassed=" + bluetoothPassed);

                                    if (!done) {
                                        if (scanResults.size() == 0) {
                                            //PPApplication.logE("[BTScan] EventsHandler.doHandleEvents", "scanResult is empty");

                                            if (event._eventPreferencesBluetooth._connectionType == EventPreferencesBluetooth.CTYPE_NOT_NEARBY)
                                                wifiPassed = true;
                                        }
                                    }

                                } //else
                                //PPApplication.logE("[BTScan] EventsHandler.doHandleEvents", "scanResults == null");
                            }
                        }
                    }
                }

                //PPApplication.logE("[BTScan] EventsHandler.doHandleEvents", "bluetoothPassed=" + bluetoothPassed);

                if (!notAllowedBluetooth) {
                    if (bluetoothPassed)
                        event._eventPreferencesBluetooth.setSensorPassed(EventPreferences.SENSOR_PASSED_PASSED);
                    else
                        event._eventPreferencesBluetooth.setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);
                }
            } else
                notAllowedBluetooth = true;
            int newSensorPassed = event._eventPreferencesBluetooth.getSensorPassed() & (~EventPreferences.SENSOR_PASSED_WAITING);
            if (oldSensorPassed != newSensorPassed) {
                event._eventPreferencesBluetooth.setSensorPassed(newSensorPassed);
                DatabaseHandler.getInstance(context).updateEventSensorPassed(event, DatabaseHandler.ETYPE_BLUETOOTH);
            }
        }

        if (event._eventPreferencesSMS._enabled) {
            int oldSensorPassed = event._eventPreferencesSMS.getSensorPassed();
            if ((Event.isEventPreferenceAllowed(EventPreferencesSMS.PREF_EVENT_SMS_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED)
                // permissions are checked in EditorProfilesActivity.displayRedTextToPreferencesNotification()
                /*&& Permissions.checkEventSMSContacts(context, event, null)*/
                /* moved to Extender && Permissions.checkEventSMSBroadcast(context, event, null)*/) {
                // compute start time

                if (event._eventPreferencesSMS._startTime > 0) {
                    //PPApplication.logE("[SMS sensor] EventsHandler.doHandleEvents", "startTime > 0");

                    int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();
                    long startTime = event._eventPreferencesSMS._startTime - gmtOffset;

                    /*if (PPApplication.logEnabled()) {
                        SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                        String alarmTimeS = sdf.format(startTime);
                        PPApplication.logE("[SMS sensor] EventsHandler.doHandleEvents", "startTime=" + alarmTimeS);
                    }*/

                    // compute end datetime
                    long endAlarmTime = event._eventPreferencesSMS.computeAlarm();
                    /*if (PPApplication.logEnabled()) {
                        SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                        String alarmTimeS = sdf.format(endAlarmTime);
                        PPApplication.logE("[SMS sensor] EventsHandler.doHandleEvents", "endAlarmTime=" + alarmTimeS);
                    }*/

                    Calendar now = Calendar.getInstance();
                    long nowAlarmTime = now.getTimeInMillis();
                    /*if (PPApplication.logEnabled()) {
                        SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                        String alarmTimeS = sdf.format(nowAlarmTime);
                        PPApplication.logE("[SMS sensor] EventsHandler.doHandleEvents", "nowAlarmTime=" + alarmTimeS);
                    }*/

                    if (sensorType.equals(EventsHandler.SENSOR_TYPE_SMS))
                        smsPassed = true;
                    else if (!event._eventPreferencesSMS._permanentRun) {
                        //PPApplication.logE("[SMS sensor] EventsHandler.doHandleEvents", "sensorType=" + sensorType);
                        if (sensorType.equals(EventsHandler.SENSOR_TYPE_SMS_EVENT_END))
                            smsPassed = false;
                        else
                            smsPassed = ((nowAlarmTime >= startTime) && (nowAlarmTime < endAlarmTime));
                        //PPApplication.logE("[SMS sensor] EventsHandler.doHandleEvents", "smsPassed=" + smsPassed);
                    } else {
                        smsPassed = nowAlarmTime >= startTime;
                    }
                } else {
                    //PPApplication.logE("[SMS sensor] EventsHandler.doHandleEvents", "startTime == 0");
                    smsPassed = false;
                }

                if (!smsPassed) {
                    event._eventPreferencesSMS._startTime = 0;
                    //if ((event != null) && (event._name != null) && (event._name.equals("SMS event")))
                    //    PPApplication.logE("[SMS sensor] EventsHandler.doHandleEvents", "startTime="+event._eventPreferencesSMS._startTime);
                    DatabaseHandler.getInstance(context).updateSMSStartTime(event);
                }

                if (!notAllowedSms) {
                    if (smsPassed)
                        event._eventPreferencesSMS.setSensorPassed(EventPreferences.SENSOR_PASSED_PASSED);
                    else
                        event._eventPreferencesSMS.setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);
                }
            } else
                notAllowedSms = true;
            int newSensorPassed = event._eventPreferencesSMS.getSensorPassed() & (~EventPreferences.SENSOR_PASSED_WAITING);
            if (oldSensorPassed != newSensorPassed) {
                event._eventPreferencesSMS.setSensorPassed(newSensorPassed);
                DatabaseHandler.getInstance(context).updateEventSensorPassed(event, DatabaseHandler.ETYPE_SMS);
            }
        }

        if (event._eventPreferencesNotification._enabled) {
            int oldSensorPassed = event._eventPreferencesNotification.getSensorPassed();
            if ((Event.isEventPreferenceAllowed(EventPreferencesNotification.PREF_EVENT_NOTIFICATION_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED)) {
                notificationPassed = event._eventPreferencesNotification.isNotificationVisible(context);

                //PPApplication.logE("[TEST BATTERY] EventsHandler.doHandleEvents", "notificationPassed=" + notificationPassed);

                if (!notAllowedNotification) {
                    if (notificationPassed)
                        event._eventPreferencesNotification.setSensorPassed(EventPreferences.SENSOR_PASSED_PASSED);
                    else
                        event._eventPreferencesNotification.setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);
                }
            } else
                notAllowedNotification = true;
            int newSensorPassed = event._eventPreferencesNotification.getSensorPassed() & (~EventPreferences.SENSOR_PASSED_WAITING);
            if (oldSensorPassed != newSensorPassed) {
                event._eventPreferencesNotification.setSensorPassed(newSensorPassed);
                DatabaseHandler.getInstance(context).updateEventSensorPassed(event, DatabaseHandler.ETYPE_NOTIFICATION);
            }
        }


        if (event._eventPreferencesApplication._enabled) {
            int oldSensorPassed = event._eventPreferencesApplication.getSensorPassed();
            if ((Event.isEventPreferenceAllowed(EventPreferencesApplication.PREF_EVENT_APPLICATION_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED)) {
                applicationPassed = false;

                if (PPPExtenderBroadcastReceiver.isEnabled(context.getApplicationContext(), PPApplication.VERSION_CODE_EXTENDER_3_0)) {
                    String foregroundApplication = ApplicationPreferences.prefApplicationInForeground;

                    if (!foregroundApplication.isEmpty()) {
                        String[] splits = event._eventPreferencesApplication._applications.split("\\|");
                        for (String split : splits) {
                            String packageName = Application.getPackageName(split);

                            if (foregroundApplication.equals(packageName)) {
                                applicationPassed = true;
                                break;
                            }
                        }
                    } else
                        notAllowedApplication = true;
                } else
                    notAllowedApplication = true;

                if (!notAllowedApplication) {
                    if (applicationPassed)
                        event._eventPreferencesApplication.setSensorPassed(EventPreferences.SENSOR_PASSED_PASSED);
                    else
                        event._eventPreferencesApplication.setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);
                }
            } else
                notAllowedApplication = true;
            int newSensorPassed = event._eventPreferencesApplication.getSensorPassed() & (~EventPreferences.SENSOR_PASSED_WAITING);
            if (oldSensorPassed != newSensorPassed) {
                event._eventPreferencesApplication.setSensorPassed(newSensorPassed);
                DatabaseHandler.getInstance(context).updateEventSensorPassed(event, DatabaseHandler.ETYPE_APPLICATION);
            }
        }

        if (event._eventPreferencesLocation._enabled) {
            int oldSensorPassed = event._eventPreferencesLocation.getSensorPassed();
            if ((Event.isEventPreferenceAllowed(EventPreferencesLocation.PREF_EVENT_LOCATION_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED)
                // permissions are checked in EditorProfilesActivity.displayRedTextToPreferencesNotification()
                /*&& Permissions.checkEventLocation(context, event, null)*/) {
                if (!ApplicationPreferences.applicationEventLocationEnableScanning) {
                    //if (forRestartEvents)
                    //    locationPassed = (EventPreferences.SENSOR_PASSED_PASSED & event._eventPreferencesLocation.getSensorPassed()) == EventPreferences.SENSOR_PASSED_PASSED;
                    //else {
                    // not allowed for disabled location scanner
                    //    PPApplication.logE("[GeoSensor] EventsHandler.doHandleEvents", "ignore for disabled scanner");
                    //    notAllowedLocation = true;
                    //}
                    locationPassed = false;
                } else {
                    //PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                    if (!PPApplication.isScreenOn && ApplicationPreferences.applicationEventLocationScanOnlyWhenScreenIsOn) {
                        if (forRestartEvents)
                            locationPassed = (EventPreferences.SENSOR_PASSED_PASSED & event._eventPreferencesLocation.getSensorPassed()) == EventPreferences.SENSOR_PASSED_PASSED;
                        else {
                            // not allowed for screen Off
                            //PPApplication.logE("[GeoSensor] EventsHandler.doHandleEvents", "ignore for screen off");
                            notAllowedLocation = true;
                        }
                    } else {
                        synchronized (PPApplication.geofenceScannerMutex) {
                            if ((PhoneProfilesService.getInstance() != null) && PhoneProfilesService.getInstance().isGeofenceScannerStarted() &&
                                    PhoneProfilesService.getInstance().getGeofencesScanner().mTransitionsUpdated) {

                                /*if (PPApplication.logEnabled()) {
                                    PPApplication.logE("[GeoSensor] EventsHandler.doHandleEvents", "--------");
                                    PPApplication.logE("[GeoSensor] EventsHandler.doHandleEvents", "_eventPreferencesLocation._geofences=" + event._eventPreferencesLocation._geofences);
                                }*/

                                String[] splits = event._eventPreferencesLocation._geofences.split("\\|");
                                boolean[] passed = new boolean[splits.length];

                                int i = 0;
                                for (String _geofence : splits) {
                                    passed[i] = false;
                                    if (!_geofence.isEmpty()) {
                                        //PPApplication.logE("[GeoSensor] EventsHandler.doHandleEvents", "geofence=" + DatabaseHandler.getInstance(context).getGeofenceName(Long.valueOf(_geofence)));

                                        int geofenceTransition = DatabaseHandler.getInstance(context).getGeofenceTransition(Long.parseLong(_geofence));
                                        /*if (geofenceTransition == com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_ENTER)
                                            PPApplication.logE("[GeoSensor] EventsHandler.doHandleEvents", "transitionType=GEOFENCE_TRANSITION_ENTER");
                                        else
                                            PPApplication.logE("[GeoSensor] EventsHandler.doHandleEvents", "transitionType=GEOFENCE_TRANSITION_EXIT");*/

                                        if (geofenceTransition == com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_ENTER) {
                                            passed[i] = true;
                                        }
                                    }
                                    ++i;
                                }

                                if (event._eventPreferencesLocation._whenOutside) {
                                    // all locations must not be passed
                                    locationPassed = true;
                                    for (boolean pass : passed) {
                                        if (pass) {
                                            locationPassed = false;
                                            break;
                                        }
                                    }
                                }
                                else {
                                    // one location must be passed
                                    locationPassed = false;
                                    for (boolean pass : passed) {
                                        if (pass) {
                                            locationPassed = true;
                                            break;
                                        }
                                    }
                                }
                                //PPApplication.logE("[GeoSensor] EventsHandler.doHandleEvents", "locationPassed=" + locationPassed);

                            } else {
                                notAllowedLocation = true;
                            }
                        }
                    }
                }

                if (!notAllowedLocation) {
                    if (locationPassed)
                        event._eventPreferencesLocation.setSensorPassed(EventPreferences.SENSOR_PASSED_PASSED);
                    else
                        event._eventPreferencesLocation.setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);
                }
            } else
                notAllowedLocation = true;
            int newSensorPassed = event._eventPreferencesLocation.getSensorPassed() & (~EventPreferences.SENSOR_PASSED_WAITING);
            if (oldSensorPassed != newSensorPassed) {
                event._eventPreferencesLocation.setSensorPassed(newSensorPassed);
                DatabaseHandler.getInstance(context).updateEventSensorPassed(event, DatabaseHandler.ETYPE_LOCATION);
            }
        }

        if (event._eventPreferencesOrientation._enabled) {
            int oldSensorPassed = event._eventPreferencesOrientation.getSensorPassed();
            if ((Event.isEventPreferenceAllowed(EventPreferencesOrientation.PREF_EVENT_ORIENTATION_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED)) {
                //PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                boolean inCall = false;
                TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                if (telephony != null) {
                    int callState = telephony.getCallState();
                    inCall = (callState == TelephonyManager.CALL_STATE_RINGING) || (callState == TelephonyManager.CALL_STATE_OFFHOOK);
                }
                if (inCall) {
                    // not allowed changes during call
                    notAllowedOrientation = true;
                } else if (!ApplicationPreferences.applicationEventOrientationEnableScanning) {
                    //if (forRestartEvents)
                    //    orientationPassed = (EventPreferences.SENSOR_PASSED_PASSED & event._eventPreferencesOrientation.getSensorPassed()) == EventPreferences.SENSOR_PASSED_PASSED;
                    //else
                    // not allowed for disabled orientation scanner
                    //    notAllowedOrientation = true;
                    orientationPassed = false;
                } else if (!PPApplication.isScreenOn && ApplicationPreferences.applicationEventOrientationScanOnlyWhenScreenIsOn) {
                    if (forRestartEvents)
                        orientationPassed = (EventPreferences.SENSOR_PASSED_PASSED & event._eventPreferencesOrientation.getSensorPassed()) == EventPreferences.SENSOR_PASSED_PASSED;
                    else
                        // not allowed for screen Off
                        notAllowedOrientation = true;
                } else {
                    synchronized (PPApplication.orientationScannerMutex) {
                        if ((PhoneProfilesService.getInstance() != null) && PhoneProfilesService.getInstance().isOrientationScannerStarted()) {
                            PPApplication.startHandlerThreadOrientationScanner();
                            boolean lApplicationPassed = false;
                            if (!event._eventPreferencesOrientation._ignoredApplications.isEmpty()) {
                                if (PPPExtenderBroadcastReceiver.isEnabled(context.getApplicationContext(), PPApplication.VERSION_CODE_EXTENDER_3_0)) {
                                    String foregroundApplication = ApplicationPreferences.prefApplicationInForeground;
                                    if (!foregroundApplication.isEmpty()) {
                                        String[] splits = event._eventPreferencesOrientation._ignoredApplications.split("\\|");
                                        for (String split : splits) {
                                            String packageName = Application.getPackageName(split);

                                            if (foregroundApplication.equals(packageName)) {
                                                lApplicationPassed = true;
                                                break;
                                            }
                                        }
                                    }
                                } else
                                    notAllowedOrientation = true;
                            }
                            if (!lApplicationPassed) {
                                boolean lDisplayPassed = false;
                                boolean lSidePassed = false;

                                boolean hasAccelerometer = PPApplication.accelerometerSensor != null;
                                boolean hasMagneticField = PPApplication.magneticFieldSensor != null;
                                boolean hasProximity = PPApplication.proximitySensor != null;
                                boolean hasLight = PPApplication.lightSensor != null;

                                boolean enabledAll = (hasAccelerometer) && (hasMagneticField);

                                boolean configuredDisplay = false;
                                if (hasAccelerometer) {
                                    if (!event._eventPreferencesOrientation._display.isEmpty()) {
                                        String[] splits = event._eventPreferencesOrientation._display.split("\\|");
                                        if (splits.length > 0) {
                                            configuredDisplay = true;
                                            //lDisplayPassed = false;
                                            for (String split : splits) {
                                                try {
                                                    int side = Integer.parseInt(split);
                                                    if (side == PPApplication.handlerThreadOrientationScanner.mDisplayUp) {
                                                        lDisplayPassed = true;
                                                        break;
                                                    }
                                                } catch (Exception ignored) {
                                                }
                                            }
                                        }
                                    }
                                }

                                boolean configuredSide = false;
                                if (enabledAll) {
                                    if (!event._eventPreferencesOrientation._sides.isEmpty()) {
                                        String[] splits = event._eventPreferencesOrientation._sides.split("\\|");
                                        if (splits.length > 0) {
                                            configuredSide = true;
                                            //lSidePassed = false;
                                            for (String split : splits) {
                                                try {
                                                    int side = Integer.parseInt(split);
                                                    if (side == OrientationScannerHandlerThread.DEVICE_ORIENTATION_HORIZONTAL) {
                                                        if (PPApplication.handlerThreadOrientationScanner.mSideUp == PPApplication.handlerThreadOrientationScanner.mDisplayUp) {
                                                            lSidePassed = true;
                                                            break;
                                                        }
                                                    } else {
                                                        if (side == PPApplication.handlerThreadOrientationScanner.mSideUp) {
                                                            lSidePassed = true;
                                                            break;
                                                        }
                                                    }
                                                } catch (Exception ignored) {
                                                }
                                            }
                                        }
                                    }
                                }

                                boolean lDistancePassed = false;
                                boolean configuredDistance = false;
                                if (hasProximity) {
                                    if (event._eventPreferencesOrientation._distance != 0) {
                                        configuredDistance = true;
                                        lDistancePassed = event._eventPreferencesOrientation._distance == PPApplication.handlerThreadOrientationScanner.mDeviceDistance;
                                    }
                                }

                                boolean lLightPassed = false;
                                boolean configuredLight = false;
                                if (hasLight) {
                                    if (event._eventPreferencesOrientation._checkLight) {
                                        configuredLight = true;
                                        int light = PPApplication.handlerThreadOrientationScanner.mLight;
                                        int min = Integer.parseInt(event._eventPreferencesOrientation._lightMin);
                                        int max = Integer.parseInt(event._eventPreferencesOrientation._lightMax);
                                        lLightPassed = (light >= min) && (light <= max);
                                        /*if (PPApplication.logEnabled()) {
                                            PPApplication.logE("[OriSensor] EventsHandler.doHandleEvents", "light=" + light);
                                            PPApplication.logE("[OriSensor] EventsHandler.doHandleEvents", "min=" + min);
                                            PPApplication.logE("[OriSensor] EventsHandler.doHandleEvents", "max=" + max);
                                        }*/
                                    }
                                }

                                /*if (PPApplication.logEnabled()) {
                                    PPApplication.logE("[OriSensor] EventsHandler.doHandleEvents", "configuredDisplay=" + configuredDisplay);
                                    PPApplication.logE("[OriSensor] EventsHandler.doHandleEvents", "configuredSide=" + configuredSide);
                                    PPApplication.logE("[OriSensor] EventsHandler.doHandleEvents", "configuredDistance=" + configuredDistance);
                                    PPApplication.logE("[OriSensor] EventsHandler.doHandleEvents", "configuredLight=" + configuredLight);

                                    PPApplication.logE("[OriSensor] EventsHandler.doHandleEvents", "hasAccelerometer=" + hasAccelerometer);
                                    PPApplication.logE("[OriSensor] EventsHandler.doHandleEvents", "hasMagneticField=" + hasMagneticField);
                                    PPApplication.logE("[OriSensor] EventsHandler.doHandleEvents", "hasProximity=" + hasProximity);
                                    PPApplication.logE("[OriSensor] EventsHandler.doHandleEvents", "hasLight=" + hasLight);

                                    PPApplication.logE("[OriSensor] EventsHandler.doHandleEvents", "lDisplayPassed=" + lDisplayPassed);
                                    PPApplication.logE("[OriSensor] EventsHandler.doHandleEvents", "lSidePassed=" + lSidePassed);
                                    PPApplication.logE("[OriSensor] EventsHandler.doHandleEvents", "lDistancePassed=" + lDistancePassed);
                                    PPApplication.logE("[OriSensor] EventsHandler.doHandleEvents", "lLightPassed=" + lLightPassed);
                                }*/

                                if (configuredDisplay || configuredSide || configuredDistance || configuredLight) {
                                    orientationPassed = true;
                                    if (configuredDisplay)
                                        orientationPassed = orientationPassed && lDisplayPassed;
                                    if (configuredSide)
                                        orientationPassed = orientationPassed && lSidePassed;
                                    if (configuredDistance)
                                        orientationPassed = orientationPassed && lDistancePassed;
                                    if (configuredLight)
                                        orientationPassed = orientationPassed && lLightPassed;
                                }
                                else
                                    notAllowedOrientation = true;
                                //orientationPassed = lDisplayPassed || lSidePassed || lDistancePassed || lLightPassed;
                            }
                        } else {
                            notAllowedOrientation = true;
                        }
                    }
                }

                if (!notAllowedOrientation) {
                    if (orientationPassed)
                        event._eventPreferencesOrientation.setSensorPassed(EventPreferences.SENSOR_PASSED_PASSED);
                    else
                        event._eventPreferencesOrientation.setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);
                }
            } else
                notAllowedOrientation = true;
            int newSensorPassed = event._eventPreferencesOrientation.getSensorPassed() & (~EventPreferences.SENSOR_PASSED_WAITING);
            if (oldSensorPassed != newSensorPassed) {
                event._eventPreferencesOrientation.setSensorPassed(newSensorPassed);
                DatabaseHandler.getInstance(context).updateEventSensorPassed(event, DatabaseHandler.ETYPE_ORIENTATION);
            }
        }

        if (event._eventPreferencesMobileCells._enabled) {
            int oldSensorPassed = event._eventPreferencesMobileCells.getSensorPassed();
            if ((Event.isEventPreferenceAllowed(EventPreferencesMobileCells.PREF_EVENT_MOBILE_CELLS_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED)
                // permissions are checked in EditorProfilesActivity.displayRedTextToPreferencesNotification()
                /*&& Permissions.checkEventLocation(context, event, null)*/) {
                if (!ApplicationPreferences.applicationEventMobileCellEnableScanning) {
                    //if (forRestartEvents)
                    //    mobileCellPassed = (EventPreferences.SENSOR_PASSED_PASSED & event._eventPreferencesMobileCells.getSensorPassed()) == EventPreferences.SENSOR_PASSED_PASSED;
                    //else
                    // not allowed for disabled mobile cells scanner
                    //    notAllowedMobileCell = true;
                    mobileCellPassed = false;
                } else {
                    //PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                    if (!PPApplication.isScreenOn && ApplicationPreferences.applicationEventMobileCellScanOnlyWhenScreenIsOn) {
                        if (forRestartEvents)
                            mobileCellPassed = (EventPreferences.SENSOR_PASSED_PASSED & event._eventPreferencesMobileCells.getSensorPassed()) == EventPreferences.SENSOR_PASSED_PASSED;
                        else
                            // not allowed for screen Off
                            notAllowedMobileCell = true;
                    } else {
                        synchronized (PPApplication.phoneStateScannerMutex) {
                            if ((PhoneProfilesService.getInstance() != null) && PhoneProfilesService.getInstance().isPhoneStateScannerStarted()) {
                                if (PhoneStateScanner.isValidCellId(PhoneStateScanner.registeredCell)) {
                                    String registeredCell = Integer.toString(PhoneStateScanner.registeredCell);
                                    if (event._eventPreferencesMobileCells._whenOutside) {
                                        // all mobile cells must not be registered
                                        String[] splits = event._eventPreferencesMobileCells._cells.split("\\|");
                                        mobileCellPassed = true;
                                        for (String cell : splits) {
                                            if (cell.equals(registeredCell)) {
                                                // one of cells in configuration is registered
                                                mobileCellPassed = false;
                                                break;
                                            }
                                        }
                                    }
                                    else {
                                        // one mobile cell must be registered
                                        String[] splits = event._eventPreferencesMobileCells._cells.split("\\|");
                                        mobileCellPassed = false;
                                        for (String cell : splits) {
                                            if (cell.equals(registeredCell)) {
                                                // one of cells in configuration is registered
                                                mobileCellPassed = true;
                                                break;
                                            }
                                        }
                                    }
                                } else
                                    notAllowedMobileCell = true;

                            } else
                                notAllowedMobileCell = true;
                        }
                    }
                }

                if (!notAllowedMobileCell) {
                    if (mobileCellPassed)
                        event._eventPreferencesMobileCells.setSensorPassed(EventPreferences.SENSOR_PASSED_PASSED);
                    else
                        event._eventPreferencesMobileCells.setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);
                }
            } else
                notAllowedMobileCell = true;
            int newSensorPassed = event._eventPreferencesMobileCells.getSensorPassed() & (~EventPreferences.SENSOR_PASSED_WAITING);
            if (oldSensorPassed != newSensorPassed) {
                event._eventPreferencesMobileCells.setSensorPassed(newSensorPassed);
                DatabaseHandler.getInstance(context).updateEventSensorPassed(event, DatabaseHandler.ETYPE_MOBILE_CELLS);
            }
        }

        if (event._eventPreferencesNFC._enabled) {
            int oldSensorPassed = event._eventPreferencesNFC.getSensorPassed();
            if ((Event.isEventPreferenceAllowed(EventPreferencesNFC.PREF_EVENT_NFC_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED)) {
                // compute start time

                if (event._eventPreferencesNFC._startTime > 0) {
                    int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();
                    long startTime = event._eventPreferencesNFC._startTime - gmtOffset;

                    /*if (PPApplication.logEnabled()) {
                        SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                        String alarmTimeS = sdf.format(startTime);
                        PPApplication.logE("EventsHandler.doHandleEvents", "startTime=" + alarmTimeS);
                    }*/

                    // compute end datetime
                    long endAlarmTime = event._eventPreferencesNFC.computeAlarm();
                    /*if (PPApplication.logEnabled()) {
                        SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                        String alarmTimeS = sdf.format(endAlarmTime);
                        PPApplication.logE("EventsHandler.doHandleEvents", "endAlarmTime=" + alarmTimeS);
                    }*/

                    Calendar now = Calendar.getInstance();
                    long nowAlarmTime = now.getTimeInMillis();
                    /*if (PPApplication.logEnabled()) {
                        SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                        String alarmTimeS = sdf.format(nowAlarmTime);
                        PPApplication.logE("EventsHandler.doHandleEvents", "nowAlarmTime=" + alarmTimeS);
                    }*/

                    if (sensorType.equals(EventsHandler.SENSOR_TYPE_NFC_TAG))
                        nfcPassed = true;
                    else if (!event._eventPreferencesNFC._permanentRun) {
                        if (sensorType.equals(EventsHandler.SENSOR_TYPE_NFC_EVENT_END))
                            nfcPassed = false;
                        else
                            nfcPassed = ((nowAlarmTime >= startTime) && (nowAlarmTime < endAlarmTime));
                    } else
                        nfcPassed = nowAlarmTime >= startTime;
                } else
                    nfcPassed = false;

                if (!nfcPassed) {
                    event._eventPreferencesNFC._startTime = 0;
                    DatabaseHandler.getInstance(context).updateNFCStartTime(event);
                }

                if (!notAllowedNfc) {
                    if (nfcPassed)
                        event._eventPreferencesNFC.setSensorPassed(EventPreferences.SENSOR_PASSED_PASSED);
                    else
                        event._eventPreferencesNFC.setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);
                }

            } else
                notAllowedNfc = true;
            int newSensorPassed = event._eventPreferencesNFC.getSensorPassed() & (~EventPreferences.SENSOR_PASSED_WAITING);
            if (oldSensorPassed != newSensorPassed) {
                event._eventPreferencesNFC.setSensorPassed(newSensorPassed);
                DatabaseHandler.getInstance(context).updateEventSensorPassed(event, DatabaseHandler.ETYPE_NFC);
            }
        }

        if (event._eventPreferencesRadioSwitch._enabled) {
            int oldSensorPassed = event._eventPreferencesRadioSwitch.getSensorPassed();
            if ((Event.isEventPreferenceAllowed(EventPreferencesRadioSwitch.PREF_EVENT_RADIO_SWITCH_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED)) {
                radioSwitchPassed = true;
                boolean tested = false;

                if ((event._eventPreferencesRadioSwitch._wifi == 1 || event._eventPreferencesRadioSwitch._wifi == 2)
                        && PPApplication.hasSystemFeature(context, PackageManager.FEATURE_WIFI)) {

                    if (!(ApplicationPreferences.prefEventWifiScanRequest ||
                            ApplicationPreferences.prefEventWifiWaitForResult ||
                            ApplicationPreferences.prefEventWifiEnabledForScan)) {
                        // ignore for wifi scanning

                        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                        int wifiState = wifiManager.getWifiState();
                        boolean enabled = ((wifiState == WifiManager.WIFI_STATE_ENABLED) || (wifiState == WifiManager.WIFI_STATE_ENABLING));
                        //PPApplication.logE("-###- EventsHandler.doHandleEvents", "wifiState=" + enabled);
                        tested = true;
                        if (event._eventPreferencesRadioSwitch._wifi == 1)
                            radioSwitchPassed = radioSwitchPassed && enabled;
                        else
                            radioSwitchPassed = radioSwitchPassed && !enabled;
                    } else
                        notAllowedRadioSwitch = true;
                }

                if ((event._eventPreferencesRadioSwitch._bluetooth == 1 || event._eventPreferencesRadioSwitch._bluetooth == 2)
                        && PPApplication.hasSystemFeature(context, PackageManager.FEATURE_BLUETOOTH)) {

                    if (!(ApplicationPreferences.prefEventBluetoothScanRequest ||
                            ApplicationPreferences.prefEventBluetoothLEScanRequest ||
                            ApplicationPreferences.prefEventBluetoothWaitForResult ||
                            ApplicationPreferences.prefEventBluetoothLEWaitForResult ||
                            ApplicationPreferences.prefEventBluetoothEnabledForScan)) {
                        // ignore for bluetooth scanning


                        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); //BluetoothScanWorker.getBluetoothAdapter(context);
                        if (bluetoothAdapter != null) {
                            boolean enabled = bluetoothAdapter.isEnabled();
                            //PPApplication.logE("-###- EventsHandler.doHandleEvents", "bluetoothState=" + enabled);
                            tested = true;
                            if (event._eventPreferencesRadioSwitch._bluetooth == 1)
                                radioSwitchPassed = radioSwitchPassed && enabled;
                            else
                                radioSwitchPassed = radioSwitchPassed && !enabled;
                        }
                    } else
                        notAllowedRadioSwitch = true;
                }

                if ((event._eventPreferencesRadioSwitch._mobileData == 1 || event._eventPreferencesRadioSwitch._mobileData == 2)
                        && PPApplication.hasSystemFeature(context, PackageManager.FEATURE_TELEPHONY)) {

                    boolean enabled = ActivateProfileHelper.isMobileData(context);
                    //PPApplication.logE("-###- EventsHandler.doHandleEvents", "mobileDataState=" + enabled);
                    tested = true;
                    if (event._eventPreferencesRadioSwitch._mobileData == 1)
                        radioSwitchPassed = radioSwitchPassed && enabled;
                    else
                        radioSwitchPassed = radioSwitchPassed && !enabled;
                }

                if ((event._eventPreferencesRadioSwitch._gps == 1 || event._eventPreferencesRadioSwitch._gps == 2)
                        && PPApplication.hasSystemFeature(context, PackageManager.FEATURE_LOCATION_GPS)) {

                    boolean enabled;
                    /*if (android.os.Build.VERSION.SDK_INT < 19)
                        enabled = Settings.Secure.isLocationProviderEnabled(context.getContentResolver(), LocationManager.GPS_PROVIDER);
                    else {*/
                    LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                    enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                    //}
                    //PPApplication.logE("-###- EventsHandler.doHandleEvents", "gpsState=" + enabled);
                    tested = true;
                    if (event._eventPreferencesRadioSwitch._gps == 1)
                        radioSwitchPassed = radioSwitchPassed && enabled;
                    else
                        radioSwitchPassed = radioSwitchPassed && !enabled;
                }

                if ((event._eventPreferencesRadioSwitch._nfc == 1 || event._eventPreferencesRadioSwitch._nfc == 2)
                        && PPApplication.hasSystemFeature(context, PackageManager.FEATURE_NFC)) {

                    NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(context);
                    if (nfcAdapter != null) {
                        boolean enabled = nfcAdapter.isEnabled();
                        //PPApplication.logE("-###- EventsHandler.doHandleEvents", "nfcState=" + enabled);
                        tested = true;
                        if (event._eventPreferencesRadioSwitch._nfc == 1)
                            radioSwitchPassed = radioSwitchPassed && enabled;
                        else
                            radioSwitchPassed = radioSwitchPassed && !enabled;
                    }
                }

                if (event._eventPreferencesRadioSwitch._airplaneMode == 1 || event._eventPreferencesRadioSwitch._airplaneMode == 2) {

                    boolean enabled = ActivateProfileHelper.isAirplaneMode(context);
                    //PPApplication.logE("-###- EventsHandler.doHandleEvents", "airplaneModeState=" + enabled);
                    tested = true;
                    if (event._eventPreferencesRadioSwitch._airplaneMode == 1)
                        radioSwitchPassed = radioSwitchPassed && enabled;
                    else
                        radioSwitchPassed = radioSwitchPassed && !enabled;
                }

                radioSwitchPassed = radioSwitchPassed && tested;

                if (!notAllowedRadioSwitch) {
                    if (radioSwitchPassed)
                        event._eventPreferencesRadioSwitch.setSensorPassed(EventPreferences.SENSOR_PASSED_PASSED);
                    else
                        event._eventPreferencesRadioSwitch.setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);
                }
            } else
                notAllowedRadioSwitch = true;
            int newSensorPassed = event._eventPreferencesRadioSwitch.getSensorPassed() & (~EventPreferences.SENSOR_PASSED_WAITING);
            if (oldSensorPassed != newSensorPassed) {
                event._eventPreferencesRadioSwitch.setSensorPassed(newSensorPassed);
                DatabaseHandler.getInstance(context).updateEventSensorPassed(event, DatabaseHandler.ETYPE_RADIO_SWITCH);
            }
        }

        if (event._eventPreferencesAlarmClock._enabled) {
            int oldSensorPassed = event._eventPreferencesAlarmClock.getSensorPassed();
            if (Event.isEventPreferenceAllowed(EventPreferencesAlarmClock.PREF_EVENT_ALARM_CLOCK_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                // compute start time

                if (event._eventPreferencesAlarmClock._startTime > 0) {
                    int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();
                    long startTime = event._eventPreferencesAlarmClock._startTime - gmtOffset;

                    /*if (PPApplication.logEnabled()) {
                        SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                        String alarmTimeS = sdf.format(startTime);
                        PPApplication.logE("EventsHandler.doHandleEvents", "startTime=" + alarmTimeS);
                    }*/

                    // compute end datetime
                    long endAlarmTime = event._eventPreferencesAlarmClock.computeAlarm();
                    /*if (PPApplication.logEnabled()) {
                        SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                        String alarmTimeS = sdf.format(endAlarmTime);
                        PPApplication.logE("EventsHandler.doHandleEvents", "endAlarmTime=" + alarmTimeS);
                    }*/

                    Calendar now = Calendar.getInstance();
                    long nowAlarmTime = now.getTimeInMillis();
                    /*if (PPApplication.logEnabled()) {
                        SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                        String alarmTimeS = sdf.format(nowAlarmTime);
                        PPApplication.logE("EventsHandler.doHandleEvents", "nowAlarmTime=" + alarmTimeS);
                    }*/

                    if (sensorType.equals(EventsHandler.SENSOR_TYPE_ALARM_CLOCK))
                        alarmClockPassed = true;
                    else if (!event._eventPreferencesAlarmClock._permanentRun) {
                        if (sensorType.equals(EventsHandler.SENSOR_TYPE_ALARM_CLOCK_EVENT_END))
                            alarmClockPassed = false;
                        else
                            alarmClockPassed = ((nowAlarmTime >= startTime) && (nowAlarmTime < endAlarmTime));
                    } else {
                        alarmClockPassed = nowAlarmTime >= startTime;
                    }
                } else
                    alarmClockPassed = false;

                if (!alarmClockPassed) {
                    event._eventPreferencesAlarmClock._startTime = 0;
                    DatabaseHandler.getInstance(context).updateAlarmClockStartTime(event);
                }

                if (!notAllowedAlarmClock) {
                    if (alarmClockPassed)
                        event._eventPreferencesAlarmClock.setSensorPassed(EventPreferences.SENSOR_PASSED_PASSED);
                    else
                        event._eventPreferencesAlarmClock.setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);
                }
            } else
                notAllowedAlarmClock = true;
            int newSensorPassed = event._eventPreferencesAlarmClock.getSensorPassed() & (~EventPreferences.SENSOR_PASSED_WAITING);
            if (oldSensorPassed != newSensorPassed) {
                event._eventPreferencesAlarmClock.setSensorPassed(newSensorPassed);
                DatabaseHandler.getInstance(context).updateEventSensorPassed(event, DatabaseHandler.ETYPE_ALARM_CLOCK);
            }
        }

        List<EventTimeline> eventTimelineList = dataWrapper.getEventTimelineList(false);

        boolean allPassed = true;
        boolean someNotAllowed = false;
        if (!notAllowedTime)
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

            if (event._name.equals("Doma")) {
                PPApplication.logE("[***] EventsHandler.doHandleEvents", "allPassed=" + allPassed);
                PPApplication.logE("[***] EventsHandler.doHandleEvents", "someNotAllowed=" + someNotAllowed);
            }

            if (event._name.equals("Doma")) {
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
                if (event._name.equals("Doma")) {
                    PPApplication.logE("[***] EventsHandler.doHandleEvents", "event.getStatus()=" + event.getStatus());
                    PPApplication.logE("[***] EventsHandler.doHandleEvents", "newEventStatus=" + newEventStatus);
                }
            }*/

            //PPApplication.logE("@@@ EventsHandler.doHandleEvents","restartEvent="+restartEvent);

            if ((event.getStatus() != newEventStatus) || forRestartEvents || event._isInDelayStart || event._isInDelayEnd) {
                //if (event._name.equals("Doma"))
                //PPApplication.logE("[***] EventsHandler.doHandleEvents", " do new event status");

                if ((newEventStatus == Event.ESTATUS_RUNNING) && (!statePause)) {
                    // do start of events, all sensors are passed

                    /*if (PPApplication.logEnabled()) {
                        if (event._name.equals("Doma")) {
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
                            //if (event._name.equals("Doma"))
                            //    PPApplication.logE("[***] EventsHandler.doHandleEvents", "event._isInDelayStart=" + event._isInDelayStart);
                            if (!event._isInDelayStart) {
                                // no delay alarm is set
                                // start event
                                long oldMergedProfile = mergedProfile._id;
                                event.startEvent(dataWrapper, eventTimelineList, /*interactive,*/ forRestartEvents, mergedProfile);
                                startProfileMerged = oldMergedProfile != mergedProfile._id;
                                //if (event._name.equals("Doma"))
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
                            event.startEvent(dataWrapper, eventTimelineList, /*interactive,*/ forRestartEvents, mergedProfile);
                            startProfileMerged = oldMergedProfile != mergedProfile._id;
                            //PPApplication.logE("[DSTART] EventsHandler.doHandleEvents", "mergedProfile=" + mergedProfile._name);
                        }
                    }
                } else if (((newEventStatus == Event.ESTATUS_PAUSE) || forRestartEvents) && statePause) {
                    // do end of events, some sensors are not passed
                    // when pausing and it is for restart events (forRestartEvent=true), force pause

                    if (newEventStatus == Event.ESTATUS_RUNNING) {
                        //event must be running, all sensors are passed
                        if (!forRestartEvents)
                            // it is not restart event, do not pause this event
                            return;
                    }

                    /*if (PPApplication.logEnabled()) {
                        if (event._name.equals("Doma")) {
                            PPApplication.logE("[***] EventsHandler.doHandleEvents", "pause event");
                            PPApplication.logE("[***] EventsHandler.doHandleEvents", "event._name=" + event._name);
                        }
                    }*/

                    if (event._isInDelayStart) {
                        //if (event._name.equals("Doma"))
                        //    PPApplication.logE("[***] EventsHandler.doHandleEvents", "isInDelayStart");
                        event.removeDelayStartAlarm(dataWrapper);
                    }
                    else {
                        if (!forDelayEndAlarm) {
                            //if (event._name.equals("Doma"))
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
                                event.pauseEvent(dataWrapper, eventTimelineList, true, false,
                                        false, true, mergedProfile, !forRestartEvents, forRestartEvents);
                                endProfileMerged = oldMergedProfile != mergedProfile._id;
                            }
                        }

                        if (forDelayEndAlarm && event._isInDelayEnd) {
                            // called for delay alarm
                            // pause event
                            long oldMergedProfile = mergedProfile._id;
                            event.pauseEvent(dataWrapper, eventTimelineList, true, false,
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

    void setEventAlarmClockParameters(long date) {
        eventAlarmClockDate = date;
    }

    void setEventCallParameters(int callEventType, String phoneNumber, long eventTime) {
        EventPreferencesCall.setEventCallEventType(context, callEventType);
        EventPreferencesCall.setEventCallEventTime(context, eventTime);
        EventPreferencesCall.setEventCallPhoneNumber(context, phoneNumber);
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
