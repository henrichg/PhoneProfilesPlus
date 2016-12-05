package sk.henrichg.phoneprofilesplus;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;

import java.util.List;

public class EventsService extends IntentService
{
    Context context;
    DataWrapper dataWrapper;
    String broadcastReceiverType;

    public static boolean restartAtEndOfEvent = false;
    public static boolean eventsProcessed;

    private int callEventType;
    public static int oldRingerMode;
    public static int oldZenMode;
    public static String oldRingtone;

    //public static final String BROADCAST_RECEIVER_TYPE_NO_BROADCAST_RECEIVER = "noBroadcastReceiver";

    public EventsService() {
        super("EventsService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        context = getApplicationContext();

        GlobalData.logE("#### EventsService.onHandleIntent", "-- start --------------------------------");

        broadcastReceiverType = intent.getStringExtra(GlobalData.EXTRA_BROADCAST_RECEIVER_TYPE);
        GlobalData.logE("#### EventsService.onHandleIntent", "broadcastReceiverType=" + broadcastReceiverType);

        restartAtEndOfEvent = false;

        // disabled for firstStartEvents
        //if (!GlobalData.getApplicationStarted(context))
        // application is not started
        //	return;

        //GlobalData.setApplicationStarted(context, true);

        GlobalData.loadPreferences(context);

        dataWrapper = new DataWrapper(context, true, false, 0);

        SharedPreferences preferences = context.getSharedPreferences(GlobalData.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
        callEventType = preferences.getInt(GlobalData.PREF_EVENT_CALL_EVENT_TYPE, PhoneCallService.CALL_EVENT_UNDEFINED);

        oldRingerMode = GlobalData.getRingerMode(context);
        oldZenMode = GlobalData.getZenMode(context);

        try {
            Uri uri = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE);
            if (uri != null)
                oldRingtone = uri.getPath();
            else
                oldRingtone = "";
        } catch (SecurityException e) {
            Permissions.grantPlayRingtoneNotificationPermissions(context, true);
            oldRingtone = "";
        } catch (Exception e) {
            oldRingtone = "";
        }

        if (PhoneProfilesService.instance != null) {
            // start of GeofenceScanner
            if (!PhoneProfilesService.isGeofenceScannerStarted())
                GlobalData.startGeofenceScanner(context);
            // start of CellTowerScanner
            if (!PhoneProfilesService.isPhoneStateStarted()) {
                GlobalData.logE("EventsService.startPhoneStateScanner", "xxx");
                //GlobalData.sendMessageToService(this, PhoneProfilesService.MSG_START_PHONE_STATE_SCANNER);
                GlobalData.startPhoneStateScanner(context);
            }
        }

        if (!GlobalData.getGlobalEventsRuning(context)) {
            // events are globally stopped

            doEndService(intent);
            dataWrapper.invalidateDataWrapper();

            return;
        }

        // start orientation listener only when events exists
        if (PhoneProfilesService.instance != null) {
            if (!PhoneProfilesService.isOrientationScannerStarted()) {
                if (dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_ORIENTATION) > 0)
                    GlobalData.startOrientationScanner(context);
            }
        }

        if (!eventsExists(broadcastReceiverType)) {
            // events not exists

            doEndService(intent);
            dataWrapper.invalidateDataWrapper();

            GlobalData.logE("@@@ EventsService.onHandleIntent", "-- end: not events found --------------------------------");

            return;
        }

        dataWrapper.getActivateProfileHelper().initialize(dataWrapper, context);

        // create a handler to post messages to the main thread
        Handler toastHandler = new Handler(getMainLooper());
        dataWrapper.setToastHandler(toastHandler);
        Handler brightnessHandler = new Handler(getMainLooper());
        dataWrapper.getActivateProfileHelper().setBrightnessHandler(brightnessHandler);

        GlobalData.logE("%%%% EventsService.onHandleIntent","broadcastReceiverType="+broadcastReceiverType);

        List<Event> eventList = dataWrapper.getEventList();

        boolean isRestart = (broadcastReceiverType.equals(RestartEventsBroadcastReceiver.BROADCAST_RECEIVER_TYPE)/* ||
                             broadcastReceiverType.equals(CalendarProviderChangedBroadcastReceiver.BROADCAST_RECEIVER_TYPE) ||
                             broadcastReceiverType.equals(SearchCalendarEventsBroadcastReceiver.BROADCAST_RECEIVER_TYPE)*/);

        boolean interactive = (!isRestart) || intent.getBooleanExtra(GlobalData.EXTRA_INTERACTIVE, false);

        if (isRestart) {
            if (intent.getBooleanExtra(GlobalData.EXTRA_UNBLOCKEVENTSRUN, false)) {
                // remove alarm for profile duration
                ProfileDurationAlarmBroadcastReceiver.removeAlarm(context);
                GlobalData.setActivatedProfileForDuration(context, 0);

                GlobalData.setEventsBlocked(context, false);
                dataWrapper.getDatabaseHandler().unblockAllEvents();
                GlobalData.setForceRunEventRunning(context, false);
            }
        }

        if (broadcastReceiverType.equals(CalendarProviderChangedBroadcastReceiver.BROADCAST_RECEIVER_TYPE) ||
                broadcastReceiverType.equals(SearchCalendarEventsBroadcastReceiver.BROADCAST_RECEIVER_TYPE)) {
            // search for calendar events
            GlobalData.logE("EventsService.onHandleIntent", "search for calendar events");
            for (Event _event : eventList) {
                if ((_event._eventPreferencesCalendar._enabled) && (_event.getStatus() != Event.ESTATUS_STOP)) {
                    GlobalData.logE("EventsService.onHandleIntent", "event._id=" + _event._id);
                    _event._eventPreferencesCalendar.saveStartEndTime(dataWrapper);
                }
            }
        }

        // "push events"
        if (isRestart) {
            // for restart events, set startTime to 0
            for (Event _event : eventList) {
                _event._eventPreferencesSMS._startTime = 0;
                dataWrapper.getDatabaseHandler().updateSMSStartTime(_event);
                _event._eventPreferencesNotification._startTime = 0;
                dataWrapper.getDatabaseHandler().updateNotificationStartTime(_event);
                _event._eventPreferencesNFC._startTime = 0;
                dataWrapper.getDatabaseHandler().updateNFCStartTime(_event);
            }
        }
        else {
            // for no-restart events, stet startTime to actual time
            if (broadcastReceiverType.equals(SMSBroadcastReceiver.BROADCAST_RECEIVER_TYPE)) {
                // search for sms events, save start time
                GlobalData.logE("EventsService.onHandleIntent", "search for sms events");
                for (Event _event : eventList) {
                    if (_event.getStatus() != Event.ESTATUS_STOP) {
                        if (_event._eventPreferencesSMS._enabled) {
                            GlobalData.logE("EventsService.onHandleIntent", "event._id=" + _event._id);
                            _event._eventPreferencesSMS.saveStartTime(dataWrapper,
                                    intent.getStringExtra(GlobalData.EXTRA_EVENT_SMS_PHONE_NUMBER),
                                    intent.getLongExtra(GlobalData.EXTRA_EVENT_SMS_DATE, 0));
                        }
                    }
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                if (broadcastReceiverType.equals(NotificationBroadcastReceiver.BROADCAST_RECEIVER_TYPE)) {
                    // search for notification events, save start time
                    GlobalData.logE("EventsService.onHandleIntent", "search for notification events");
                    for (Event _event : eventList) {
                        if (_event.getStatus() != Event.ESTATUS_STOP) {
                            if (_event._eventPreferencesNotification._enabled) {
                                GlobalData.logE("EventsService.onHandleIntent", "event._id=" + _event._id);
                        /*_event._eventPreferencesNotification.saveStartTime(dataWrapper,
                                intent.getStringExtra(GlobalData.EXTRA_EVENT_NOTIFICATION_PACKAGE_NAME),
                                intent.getLongExtra(GlobalData.EXTRA_EVENT_NOTIFICATION_TIME, 0));*/
                                if (intent.getStringExtra(GlobalData.EXTRA_EVENT_NOTIFICATION_POSTED_REMOVED).equals("posted"))
                                    _event._eventPreferencesNotification.saveStartTime(dataWrapper);

                            }
                        }
                    }
                }
            }
            if (broadcastReceiverType.equals(NFCBroadcastReceiver.BROADCAST_RECEIVER_TYPE)) {
                // search for sms events, save start time
                GlobalData.logE("EventsService.onHandleIntent", "search for nfc events");
                for (Event _event : eventList) {
                    if (_event.getStatus() != Event.ESTATUS_STOP) {
                        if (_event._eventPreferencesNFC._enabled) {
                            GlobalData.logE("EventsService.onHandleIntent", "event._id=" + _event._id);
                            _event._eventPreferencesNFC.saveStartTime(dataWrapper,
                                    intent.getStringExtra(GlobalData.EXTRA_EVENT_NFC_TAG_NAME),
                                    intent.getLongExtra(GlobalData.EXTRA_EVENT_NFC_DATE, 0));
                        }
                    }
                }
            }
        }

        boolean forDelayStartAlarm = broadcastReceiverType.equals(EventDelayStartBroadcastReceiver.BROADCAST_RECEIVER_TYPE);
        boolean forDelayEndAlarm = broadcastReceiverType.equals(EventDelayEndBroadcastReceiver.BROADCAST_RECEIVER_TYPE);

        //GlobalData.logE("@@@ EventsService.onHandleIntent","isRestart="+isRestart);
        GlobalData.logE("@@@ EventsService.onHandleIntent","forDelayStartAlarm="+forDelayStartAlarm);
        GlobalData.logE("@@@ EventsService.onHandleIntent","forDelayEndAlarm="+forDelayEndAlarm);

        // get running events count
        List<EventTimeline> _etl = dataWrapper.getEventTimelineList();
        int runningEventCount0 = _etl.size();

        // no refresh notification and widgets
        ActivateProfileHelper.lockRefresh = true;

        //BluetoothScanAlarmBroadcastReceiver.getBoundedDevicesList(context);
        //BluetoothScanAlarmBroadcastReceiver.getScanResults(context);

        Profile mergedProfile = DataWrapper.getNoinitializedProfile("", "", 0);

        //Profile activatedProfile0 = null;

        if (isRestart)
        {
            GlobalData.logE("$$$ EventsService.onHandleIntent","restart events");

            // 1. pause events
            dataWrapper.sortEventsByStartOrderDesc();
            for (Event _event : eventList)
            {
                GlobalData.logE("EventsService.onHandleIntent","state PAUSE");
                GlobalData.logE("EventsService.onHandleIntent","event._id="+_event._id);
                GlobalData.logE("EventsService.onHandleIntent","event.getStatus()="+_event.getStatus());

                if (_event.getStatus() != Event.ESTATUS_STOP)
                    // len pauzuj eventy
                    // pauzuj aj ked uz je zapauznuty
                    dataWrapper.doEventService(_event, true, true, interactive, forDelayStartAlarm, forDelayEndAlarm, true, mergedProfile, broadcastReceiverType);
            }
            // 2. start events
            dataWrapper.sortEventsByStartOrderAsc();
            for (Event _event : eventList)
            {
                GlobalData.logE("EventsService.onHandleIntent","state RUNNING");
                GlobalData.logE("EventsService.onHandleIntent","event._id="+_event._id);
                GlobalData.logE("EventsService.onHandleIntent","event.getStatus()="+_event.getStatus());

                if (_event.getStatus() != Event.ESTATUS_STOP)
                    // len spustaj eventy
                    // spustaj len ak este nebezi
                    dataWrapper.doEventService(_event, false, false, interactive, forDelayStartAlarm, forDelayEndAlarm, true, mergedProfile, broadcastReceiverType);
            }
        }
        else
        {
            GlobalData.logE("$$$ EventsService.onHandleIntent","NO restart events");

            //activatedProfile0 = dataWrapper.getActivatedProfileFromDB();

            //1. pause events
            dataWrapper.sortEventsByStartOrderDesc();
            for (Event _event : eventList)
            {
                GlobalData.logE("EventsService.onHandleIntent","state PAUSE");
                GlobalData.logE("EventsService.onHandleIntent","event._id="+_event._id);
                GlobalData.logE("EventsService.onHandleIntent","event.getStatus()="+_event.getStatus());

                if (_event.getStatus() != Event.ESTATUS_STOP)
                    // len pauzuj eventy
                    // pauzuj len ak este nie je zapauznuty
                    dataWrapper.doEventService(_event, true, false, interactive, forDelayStartAlarm, forDelayEndAlarm, false, mergedProfile, broadcastReceiverType);
            }
            //2. start events
            dataWrapper.sortEventsByStartOrderAsc();
            for (Event _event : eventList)
            {
                GlobalData.logE("EventsService.onHandleIntent","state RUNNING");
                GlobalData.logE("EventsService.onHandleIntent","event._id="+_event._id);
                GlobalData.logE("EventsService.onHandleIntent","event.getStatus()="+_event.getStatus());

                if (_event.getStatus() != Event.ESTATUS_STOP)
                    // len spustaj eventy
                    // spustaj len ak este nebezi
                    dataWrapper.doEventService(_event, false, false, interactive, forDelayStartAlarm, forDelayEndAlarm, true, mergedProfile, broadcastReceiverType);
            }
        }

        ActivateProfileHelper.lockRefresh = false;

        if (mergedProfile._id == 0)
            GlobalData.logE("$$$ EventsService.profile for activation","no profile for activation");
        else
            GlobalData.logE("$$$ EventsService.profile for activation","profileName="+mergedProfile._name);

        if ((!restartAtEndOfEvent) || isRestart) {
            // No any paused events has "Restart events" at end of event

            //////////////////
            //// when no events are running or manual activation,
            //// activate background profile when no profile is activated

            // get running events count
            List<EventTimeline> eventTimelineList = dataWrapper.getEventTimelineList();
            int runningEventCountE = eventTimelineList.size();

            Profile activatedProfile = dataWrapper.getActivatedProfileFromDB();

            if (!dataWrapper.getIsManualProfileActivation()) {
                GlobalData.logE("$$$ EventsService.onHandleIntent", "active profile is NOT activated manually");
                GlobalData.logE("$$$ EventsService.onHandleIntent", "runningEventCountE=" + runningEventCountE);
                // no manual profile activation
                if (runningEventCountE == 0) {
                    GlobalData.logE("$$$ EventsService.onHandleIntent", "no events running");
                    // no events running
                    long profileId = Long.valueOf(GlobalData.applicationBackgroundProfile);
                    if (profileId != GlobalData.PROFILE_NO_ACTIVATE) {
                        GlobalData.logE("$$$ EventsService.onHandleIntent", "default profile is set");
                        long activatedProfileId = 0;
                        if (activatedProfile != null)
                            activatedProfileId = activatedProfile._id;
                        if ((activatedProfileId != profileId) || isRestart) {
                            mergedProfile.mergeProfiles(profileId, dataWrapper);
                            GlobalData.logE("$$$ EventsService.onHandleIntent", "activated default profile");
                        }
                    }
                /*else
                if (activatedProfile == null)
                {
                    mergedProfile.mergeProfiles(0, dataWrapper);
                    GlobalData.logE("### EventsService.onHandleIntent", "not activated profile");
                }*/
                }
            } else {
                GlobalData.logE("$$$ EventsService.onHandleIntent", "active profile is activated manually");
                // manual profile activation
                long profileId = Long.valueOf(GlobalData.applicationBackgroundProfile);
                if (profileId != GlobalData.PROFILE_NO_ACTIVATE) {
                    if (activatedProfile == null) {
                        // if not profile activated, activate Default profile
                        mergedProfile.mergeProfiles(profileId, dataWrapper);
                        GlobalData.logE("$$$ EventsService.onHandleIntent", "not activated profile");
                    }
                }
            }
            ////////////////

            String eventNotificationSound = "";

            if ((!isRestart) && (runningEventCountE > runningEventCount0)) {
                // only when not restart events and running events is increased, play event notification sound

                EventTimeline eventTimeline = eventTimelineList.get(runningEventCountE - 1);
                Event event = dataWrapper.getEventById(eventTimeline._fkEvent);
                if (event != null)
                    eventNotificationSound = event._notificationSound;
            }

            GlobalData.logE("$$$ EventsService.onHandleIntent", "mergedProfile=" + mergedProfile);

            GlobalData.logE("$$$ EventsService.onHandleIntent", "mergedProfile._id=" + mergedProfile._id);
            if (mergedProfile._id != 0) {
                // activate merged profile
                GlobalData.logE("$$$ EventsService.onHandleIntent", "profileName=" + mergedProfile._name);
                GlobalData.logE("$$$ EventsService.onHandleIntent", "profileId=" + mergedProfile._id);
                GlobalData.logE("$$$ EventsService.onHandleIntent", "profile._deviceRunApplicationPackageName=" + mergedProfile._deviceRunApplicationPackageName);
                GlobalData.logE("$$$ EventsService.onHandleIntent", "interactive=" + interactive);
                dataWrapper.getDatabaseHandler().saveMergedProfile(mergedProfile);
                dataWrapper.activateProfileFromEvent(mergedProfile._id, interactive, false, true, false);

                if (PhoneProfilesService.instance != null)
                    PhoneProfilesService.instance.playEventNotificationSound(eventNotificationSound);

                // wait for profile activation
                //try { Thread.sleep(500); } catch (InterruptedException e) { }
                //SystemClock.sleep(500);
                GlobalData.sleep(500);
            } else {
                /*long prId0 = 0;
                long prId = 0;
                if (activatedProfile0 != null) prId0 = activatedProfile0._id;
                if (activatedProfile != null) prId = activatedProfile._id;
                if ((prId0 != prId) || (prId == 0))*/
                dataWrapper.updateNotificationAndWidgets(activatedProfile);

                if (PhoneProfilesService.instance != null)
                    PhoneProfilesService.instance.playEventNotificationSound(eventNotificationSound);

            }

        }

        restartAtEndOfEvent = false;
        eventsProcessed = true;

        doEndService(intent);

        // refresh GUI
        Intent refreshIntent = new Intent();
        refreshIntent.setAction(RefreshGUIBroadcastReceiver.INTENT_REFRESH_GUI);
        context.sendBroadcast(refreshIntent);

        dataWrapper.invalidateDataWrapper();

        GlobalData.logE("@@@ EventsService.onHandleIntent","-- end --------------------------------");

    }

    private boolean eventsExists(String broadcastReceiverType) {
        int eventType = 0;
        if (broadcastReceiverType.equals(BatteryEventBroadcastReceiver.BROADCAST_RECEIVER_TYPE))
            eventType = DatabaseHandler.ETYPE_BATTERY;
        else
        if (broadcastReceiverType.equals(BluetoothConnectionBroadcastReceiver.BROADCAST_RECEIVER_TYPE))
            eventType = DatabaseHandler.ETYPE_BLUETOOTHCONNECTED;
        /*else
        if (broadcastReceiverType.equals(BluetoothLEScanBroadcastReceiver.BROADCAST_RECEIVER_TYPE))
            eventType = DatabaseHandler.ETYPE_????;*/
        /*else
        if (broadcastReceiverType.equals(BluetoothScanBroadcastReceiver.BROADCAST_RECEIVER_TYPE))
            eventType = DatabaseHandler.ETYPE_????;*/
        else
        if (broadcastReceiverType.equals(BluetoothStateChangedBroadcastReceiver.BROADCAST_RECEIVER_TYPE))
            eventType = DatabaseHandler.ETYPE_BLUETOOTHCONNECTED;
        else
        if (broadcastReceiverType.equals(CalendarProviderChangedBroadcastReceiver.BROADCAST_RECEIVER_TYPE))
            eventType = DatabaseHandler.ETYPE_CALENDAR;
        else
        if (broadcastReceiverType.equals(DockConnectionBroadcastReceiver.BROADCAST_RECEIVER_TYPE))
            eventType = DatabaseHandler.ETYPE_PERIPHERAL;
        /*else
        if (broadcastReceiverType.equals(EventDelayStartBroadcastReceiver.BROADCAST_RECEIVER_TYPE))
            eventType = DatabaseHandler.ETYPE_????;*/
        /*else
        if (broadcastReceiverType.equals(EventDelayEndBroadcastReceiver.BROADCAST_RECEIVER_TYPE))
            eventType = DatabaseHandler.ETYPE_????;*/
        else
        if (broadcastReceiverType.equals(EventCalendarBroadcastReceiver.BROADCAST_RECEIVER_TYPE))
            eventType = DatabaseHandler.ETYPE_CALENDAR;
        else
        if (broadcastReceiverType.equals(EventTimeBroadcastReceiver.BROADCAST_RECEIVER_TYPE))
            eventType = DatabaseHandler.ETYPE_TIME;
        else
        if (broadcastReceiverType.equals(ForegroundApplicationChangedBroadcastReceiver.BROADCAST_RECEIVER_TYPE))
            eventType = DatabaseHandler.ETYPE_APPLICATION;
        else
        if (broadcastReceiverType.equals(HeadsetConnectionBroadcastReceiver.BROADCAST_RECEIVER_TYPE))
            eventType = DatabaseHandler.ETYPE_PERIPHERAL;
        else
        if (broadcastReceiverType.equals(NotificationBroadcastReceiver.BROADCAST_RECEIVER_TYPE))
            eventType = DatabaseHandler.ETYPE_NOTIFICATION;
        else
        if (broadcastReceiverType.equals(NotificationEventEndBroadcastReceiver.BROADCAST_RECEIVER_TYPE))
            eventType = DatabaseHandler.ETYPE_NOTIFICATION;
        else
        if (broadcastReceiverType.equals(PhoneCallBroadcastReceiver.BROADCAST_RECEIVER_TYPE))
            eventType = DatabaseHandler.ETYPE_CALL;
        /*else
        if (broadcastReceiverType.equals(RestartEventsBroadcastReceiver.BROADCAST_RECEIVER_TYPE))
            eventType = DatabaseHandler.ETYPE_???;*/
        /*else
        // call doEventService for all screen on/off changes
        if (broadcastReceiverType.equals(ScreenOnOffBroadcastReceiver.BROADCAST_RECEIVER_TYPE))
            eventType = DatabaseHandler.ETYPE_SCREEN;*/
        else
        if (broadcastReceiverType.equals(SearchCalendarEventsBroadcastReceiver.BROADCAST_RECEIVER_TYPE))
            eventType = DatabaseHandler.ETYPE_CALENDAR;
        else
        if (broadcastReceiverType.equals(SMSBroadcastReceiver.BROADCAST_RECEIVER_TYPE))
            eventType = DatabaseHandler.ETYPE_SMS;
        else
        if (broadcastReceiverType.equals(SMSEventEndBroadcastReceiver.BROADCAST_RECEIVER_TYPE))
            eventType = DatabaseHandler.ETYPE_SMS;
        else
        if (broadcastReceiverType.equals(WifiConnectionBroadcastReceiver.BROADCAST_RECEIVER_TYPE))
            eventType = DatabaseHandler.ETYPE_WIFICONNECTED;
        /*else
        if (broadcastReceiverType.equals(WifiScanBroadcastReceiver.BROADCAST_RECEIVER_TYPE))
            eventType = DatabaseHandler.ETYPE_????;*/
        else
        if (broadcastReceiverType.equals(WifiStateChangedBroadcastReceiver.BROADCAST_RECEIVER_TYPE))
            eventType = DatabaseHandler.ETYPE_WIFICONNECTED;
        /*else
        if (broadcastReceiverType.equals(DeviceIdleModeBroadcastReceiver.BROADCAST_RECEIVER_TYPE))
            eventType = DatabaseHandler.ETYPE_????;*/
        else
        if (broadcastReceiverType.equals(PowerSaveModeBroadcastReceiver.BROADCAST_RECEIVER_TYPE))
            eventType = DatabaseHandler.ETYPE_BATTERY;
        else
        if (broadcastReceiverType.equals(GeofenceScannerBroadcastReceiver.BROADCAST_RECEIVER_TYPE))
            eventType = DatabaseHandler.ETYPE_LOCATION;
        else
        if (broadcastReceiverType.equals(LocationModeChangedBroadcastReceiver.BROADCAST_RECEIVER_TYPE))
            eventType = DatabaseHandler.ETYPE_LOCATION;
        else
        if (broadcastReceiverType.equals(DeviceOrientationBroadcastReceiver.BROADCAST_RECEIVER_TYPE))
            eventType = DatabaseHandler.ETYPE_ORIENTATION;
        else
        if (broadcastReceiverType.equals(PhoneStateChangeBroadcastReceiver.BROADCAST_RECEIVER_TYPE))
            eventType = DatabaseHandler.ETYPE_MOBILE_CELLS;
        else
        if (broadcastReceiverType.equals(NFCBroadcastReceiver.BROADCAST_RECEIVER_TYPE))
            eventType = DatabaseHandler.ETYPE_NFC;
        else
        if (broadcastReceiverType.equals(NFCEventEndBroadcastReceiver.BROADCAST_RECEIVER_TYPE))
            eventType = DatabaseHandler.ETYPE_NFC;

        if (eventType > 0)
            return dataWrapper.getDatabaseHandler().getTypeEventsCount(eventType) > 0;
        else
            return true;
    }

    private void doEndService(Intent intent) {
        GlobalData.logE("EventsService.doEndService","broadcastReceiverType="+broadcastReceiverType);
        GlobalData.logE("EventsService.doEndService","callEventType="+callEventType);

        if (broadcastReceiverType.equals(PhoneCallBroadcastReceiver.BROADCAST_RECEIVER_TYPE)) {

            if (!PhoneCallService.linkUnlinkExecuted) {
                // no profile is activated from EventsService
                // link, unlink volumes for activated profile
                boolean linkUnlink = false;
                if (callEventType == PhoneCallService.CALL_EVENT_INCOMING_CALL_RINGING)
                    linkUnlink = true;
                if (callEventType == PhoneCallService.CALL_EVENT_INCOMING_CALL_ENDED)
                    linkUnlink = true;
                if (linkUnlink) {
                    Profile profile = dataWrapper.getActivatedProfile();
                    profile = GlobalData.getMappedProfile(profile, context);
                    if (profile != null) {
                        GlobalData.logE("EventsService.doEndService", "callEventType=" + callEventType);
                        Intent volumeServiceIntent = new Intent(context, ExecuteVolumeProfilePrefsService.class);
                        volumeServiceIntent.putExtra(GlobalData.EXTRA_PROFILE_ID, profile._id);
                        volumeServiceIntent.putExtra(GlobalData.EXTRA_FOR_PROFILE_ACTIVATION, false);
                        context.startService(volumeServiceIntent);
                        // wait for link/unlink
                        //try { Thread.sleep(500); } catch (InterruptedException e) { }
                        //SystemClock.sleep(500);
                        GlobalData.sleep(500);
                    }
                }
            } else
                PhoneCallService.linkUnlinkExecuted = false;

            if ((android.os.Build.VERSION.SDK_INT >= 21) && (callEventType == PhoneCallService.CALL_EVENT_INCOMING_CALL_RINGING)) {
                // start PhoneProfilesService for ringing call simulation
                Intent lIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
                lIntent.putExtra(GlobalData.EXTRA_SIMULATE_RINGING_CALL, true);
                lIntent.putExtra(GlobalData.EXTRA_OLD_RINGER_MODE, oldRingerMode);
                lIntent.putExtra(GlobalData.EXTRA_OLD_ZEN_MODE, oldZenMode);
                lIntent.putExtra(GlobalData.EXTRA_OLD_RINGTONE, oldRingtone);
                context.startService(lIntent);
            }

            if (!PhoneCallService.speakerphoneOnExecuted) {
                // no profile is activated from EventsService
                // set speakerphone ON for activated profile
                if ((callEventType == PhoneCallService.CALL_EVENT_INCOMING_CALL_ANSWERED) ||
                        (callEventType == PhoneCallService.CALL_EVENT_OUTGOING_CALL_ANSWERED)) {
                    Profile profile = dataWrapper.getActivatedProfile();
                    profile = GlobalData.getMappedProfile(profile, context);
                    PhoneCallService.setSpeakerphoneOn(profile, context);
                }
            } else
                PhoneCallService.speakerphoneOnExecuted = false;

            if ((callEventType == PhoneCallService.CALL_EVENT_INCOMING_CALL_ENDED) ||
                (callEventType == PhoneCallService.CALL_EVENT_OUTGOING_CALL_ENDED)) {
                SharedPreferences preferences = context.getSharedPreferences(GlobalData.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt(GlobalData.PREF_EVENT_CALL_EVENT_TYPE, PhoneCallService.CALL_EVENT_UNDEFINED);
                editor.putString(GlobalData.PREF_EVENT_CALL_PHONE_NUMBER, "");
                editor.commit();
            }
        }

        // completting wake
        if (broadcastReceiverType.equals(BatteryEventBroadcastReceiver.BROADCAST_RECEIVER_TYPE)) //batteryEvent
            BatteryEventBroadcastReceiver.completeWakefulIntent(intent);
        else
        if (broadcastReceiverType.equals(BluetoothConnectionBroadcastReceiver.BROADCAST_RECEIVER_TYPE)) //bluetoothConnection
            BluetoothConnectionBroadcastReceiver.completeWakefulIntent(intent);
        else
        if (broadcastReceiverType.equals(BluetoothLEScanBroadcastReceiver.BROADCAST_RECEIVER_TYPE)) //bluetoothLEScan
            BluetoothLEScanBroadcastReceiver.completeWakefulIntent(intent);
        else
        if (broadcastReceiverType.equals(BluetoothScanBroadcastReceiver.BROADCAST_RECEIVER_TYPE)) //bluetoothScan
            BluetoothScanBroadcastReceiver.completeWakefulIntent(intent);
        else
        if (broadcastReceiverType.equals(BluetoothStateChangedBroadcastReceiver.BROADCAST_RECEIVER_TYPE)) //bluetoothState
            BluetoothStateChangedBroadcastReceiver.completeWakefulIntent(intent);
        else
        if (broadcastReceiverType.equals(CalendarProviderChangedBroadcastReceiver.BROADCAST_RECEIVER_TYPE)) //calendarProviderChanged
            CalendarProviderChangedBroadcastReceiver.completeWakefulIntent(intent);
        else
        if (broadcastReceiverType.equals(DeviceIdleModeBroadcastReceiver.BROADCAST_RECEIVER_TYPE))  //deviceIdleMode
            DeviceIdleModeBroadcastReceiver.completeWakefulIntent(intent);
        else
        if (broadcastReceiverType.equals(DeviceOrientationBroadcastReceiver.BROADCAST_RECEIVER_TYPE)) //deviceOrientation
            DeviceOrientationBroadcastReceiver.completeWakefulIntent(intent);
        else
        if (broadcastReceiverType.equals(DockConnectionBroadcastReceiver.BROADCAST_RECEIVER_TYPE)) //dockConnection
            DockConnectionBroadcastReceiver.completeWakefulIntent(intent);
        else
        if (broadcastReceiverType.equals(EventCalendarBroadcastReceiver.BROADCAST_RECEIVER_TYPE)) //eventCalendarStart
            EventCalendarBroadcastReceiver.completeWakefulIntent(intent);
        else
        if (broadcastReceiverType.equals(EventDelayEndBroadcastReceiver.BROADCAST_RECEIVER_TYPE)) //eventDelayEnd
            EventDelayEndBroadcastReceiver.completeWakefulIntent(intent);
        else
        if (broadcastReceiverType.equals(EventDelayStartBroadcastReceiver.BROADCAST_RECEIVER_TYPE)) //eventDelayStart
            EventDelayStartBroadcastReceiver.completeWakefulIntent(intent);
        else
        if (broadcastReceiverType.equals(EventTimeBroadcastReceiver.BROADCAST_RECEIVER_TYPE)) //eventTimeStart
            EventTimeBroadcastReceiver.completeWakefulIntent(intent);
        else
        if (broadcastReceiverType.equals(ForegroundApplicationChangedBroadcastReceiver.BROADCAST_RECEIVER_TYPE)) //Application
            ForegroundApplicationChangedBroadcastReceiver.completeWakefulIntent(intent);
        else
        if (broadcastReceiverType.equals(GeofenceScannerBroadcastReceiver.BROADCAST_RECEIVER_TYPE)) //geofenceScanner
            GeofenceScannerBroadcastReceiver.completeWakefulIntent(intent);
        else
        if (broadcastReceiverType.equals(HeadsetConnectionBroadcastReceiver.BROADCAST_RECEIVER_TYPE)) //headsetConnection
            HeadsetConnectionBroadcastReceiver.completeWakefulIntent(intent);
        else
        if (broadcastReceiverType.equals(LocationModeChangedBroadcastReceiver.BROADCAST_RECEIVER_TYPE)) //locationModeChanged
            LocationModeChangedBroadcastReceiver.completeWakefulIntent(intent);
        else
        if (broadcastReceiverType.equals(NotificationBroadcastReceiver.BROADCAST_RECEIVER_TYPE)) //Notification
            NotificationBroadcastReceiver.completeWakefulIntent(intent);
        else
        if (broadcastReceiverType.equals(NotificationEventEndBroadcastReceiver.BROADCAST_RECEIVER_TYPE)) //NotificationAlarm
            NotificationEventEndBroadcastReceiver.completeWakefulIntent(intent);
        else
        if (broadcastReceiverType.equals(PowerSaveModeBroadcastReceiver.BROADCAST_RECEIVER_TYPE)) //powerSaveMode
            PowerSaveModeBroadcastReceiver.completeWakefulIntent(intent);
        else
        if (broadcastReceiverType.equals(RestartEventsBroadcastReceiver.BROADCAST_RECEIVER_TYPE)) //restartEvents
            RestartEventsBroadcastReceiver.completeWakefulIntent(intent);
        else
        if (broadcastReceiverType.equals(ScreenOnOffBroadcastReceiver.BROADCAST_RECEIVER_TYPE)) //screenOnOff
            ScreenOnOffBroadcastReceiver.completeWakefulIntent(intent);
        else
        if (broadcastReceiverType.equals(SearchCalendarEventsBroadcastReceiver.BROADCAST_RECEIVER_TYPE)) //searchCalendarEvents
            SearchCalendarEventsBroadcastReceiver.completeWakefulIntent(intent);
        else
        if (broadcastReceiverType.equals(SMSBroadcastReceiver.BROADCAST_RECEIVER_TYPE)) //SMS
            SMSBroadcastReceiver.completeWakefulIntent(intent);
        else
        if (broadcastReceiverType.equals(SMSEventEndBroadcastReceiver.BROADCAST_RECEIVER_TYPE)) //SMSAlarm
            SMSEventEndBroadcastReceiver.completeWakefulIntent(intent);
        else
        if (broadcastReceiverType.equals(StartEventsServiceBroadcastReceiver.BROADCAST_RECEIVER_TYPE)) //startEventsService
            StartEventsServiceBroadcastReceiver.completeWakefulIntent(intent);
        else
        if (broadcastReceiverType.equals(WifiConnectionBroadcastReceiver.BROADCAST_RECEIVER_TYPE)) //wifiConnection
            WifiConnectionBroadcastReceiver.completeWakefulIntent(intent);
        else
        if (broadcastReceiverType.equals(WifiScanBroadcastReceiver.BROADCAST_RECEIVER_TYPE)) //wifiScan
            WifiScanBroadcastReceiver.completeWakefulIntent(intent);
        else
        if (broadcastReceiverType.equals(WifiStateChangedBroadcastReceiver.BROADCAST_RECEIVER_TYPE)) //wifiState
            WifiStateChangedBroadcastReceiver.completeWakefulIntent(intent);
        else
        if (broadcastReceiverType.equals(PhoneStateChangeBroadcastReceiver.BROADCAST_RECEIVER_TYPE)) //deviceOrientation
            PhoneStateChangeBroadcastReceiver.completeWakefulIntent(intent);
        else
        if (broadcastReceiverType.equals(NFCBroadcastReceiver.BROADCAST_RECEIVER_TYPE)) //SMS
            NFCBroadcastReceiver.completeWakefulIntent(intent);
        else
        if (broadcastReceiverType.equals(NFCEventEndBroadcastReceiver.BROADCAST_RECEIVER_TYPE)) //SMSAlarm
            NFCEventEndBroadcastReceiver.completeWakefulIntent(intent);

        // this broadcast not starts service with wakefull method
        //if (broadcastReceiverType.equals(PhoneCallBroadcastReceiver.BROADCAST_RECEIVER_TYPE))
        //    PhoneCallBroadcastReceiver.completeWakefulIntent(intent);
    }

}
