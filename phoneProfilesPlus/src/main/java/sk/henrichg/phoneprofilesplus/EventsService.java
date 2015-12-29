package sk.henrichg.phoneprofilesplus;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import java.util.List;

public class EventsService extends IntentService
{
    Context context;
    DataWrapper dataWrapper;
    String broadcastReceiverType;

    public static boolean restartAtEndOfEvent = false;

    public static final String BROADCAST_RECEIVER_TYPE_NO_BROADCAST_RECEIVER = "noBroadcastReceiver";

    public EventsService() {
        super("EventsService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        context = getApplicationContext();

        GlobalData.logE("$$$ EventsService.onHandleIntent","-- start --------------------------------");

        restartAtEndOfEvent = false;

        // disabled for firstStartEvents
        //if (!GlobalData.getApplicationStarted(context))
            // application is not started
        //	return;

        GlobalData.setApplicationStarted(context, true);


        if (!GlobalData.getGlobalEventsRuning(context))
            // events are globally stopped
            return;

        GlobalData.loadPreferences(context);

        broadcastReceiverType = intent.getStringExtra(GlobalData.EXTRA_BROADCAST_RECEIVER_TYPE);

        dataWrapper = new DataWrapper(context, true, false, 0);

        if (!eventsExists(broadcastReceiverType)) {
            doEndService(intent);
            dataWrapper.invalidateDataWrapper();

            GlobalData.logE("@@@ EventsService.onHandleIntent","-- end: not events found --------------------------------");

            return;
        }

        dataWrapper.getActivateProfileHelper().initialize(dataWrapper, null, context);

        // create a handler to post messages to the main thread
        Handler toastHandler = new Handler(getMainLooper());
        dataWrapper.setToastHandler(toastHandler);
        Handler brightnessHandler = new Handler(getMainLooper());
        dataWrapper.getActivateProfileHelper().setBrightnessHandler(brightnessHandler);

        GlobalData.logE("$$$ EventsService.onHandleIntent","broadcastReceiverType="+broadcastReceiverType);

        List<Event> eventList = dataWrapper.getEventList();

        boolean isRestart = (broadcastReceiverType.equals(RestartEventsBroadcastReceiver.BROADCAST_RECEIVER_TYPE)/* ||
                             broadcastReceiverType.equals(CalendarProviderChangedBroadcastReceiver.BROADCAST_RECEIVER_TYPE) ||
                             broadcastReceiverType.equals(SearchCalendarEventsBroadcastReceiver.BROADCAST_RECEIVER_TYPE)*/);

        boolean interactive = !isRestart;

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
            broadcastReceiverType.equals(SearchCalendarEventsBroadcastReceiver.BROADCAST_RECEIVER_TYPE))
        {
            // search for calendar events
            GlobalData.logE("EventsService.onHandleIntent","search for calendar events");
            for (Event _event : eventList)
            {
                if (_event.getStatus() != Event.ESTATUS_STOP)
                {
                    if (_event._eventPreferencesCalendar._enabled)
                    {
                        GlobalData.logE("EventsService.onHandleIntent","event._id="+_event._id);
                        _event._eventPreferencesCalendar.searchEvent(context);
                    }
                }
            }
        }
        if (broadcastReceiverType.equals(SMSBroadcastReceiver.BROADCAST_RECEIVER_TYPE)) {
            // search for sms events, save start time
            GlobalData.logE("EventsService.onHandleIntent","search for sms events");
            for (Event _event : eventList)
            {
                if (_event.getStatus() != Event.ESTATUS_STOP)
                {
                    if (_event._eventPreferencesSMS._enabled)
                    {
                        GlobalData.logE("EventsService.onHandleIntent", "event._id=" + _event._id);
                        _event._eventPreferencesSMS.saveStartTime(dataWrapper,
                                intent.getStringExtra(GlobalData.EXTRA_EVENT_SMS_PHONE_NUMBER),
                                intent.getLongExtra(GlobalData.EXTRA_EVENT_SMS_DATE, 0));
                    }
                }
            }
        }
        if (broadcastReceiverType.equals(NotificationBroadcastReceiver.BROADCAST_RECEIVER_TYPE)) {
            // search for notification events, save start time
            GlobalData.logE("EventsService.onHandleIntent","search for notification events");
            for (Event _event : eventList)
            {
                if (_event.getStatus() != Event.ESTATUS_STOP)
                {
                    if ((_event._eventPreferencesNotification._enabled) && (!_event._eventPreferencesNotification._endWhenRemoved))
                    {
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

        boolean forDelayAlarm = broadcastReceiverType.equals(EventDelayBroadcastReceiver.BROADCAST_RECEIVER_TYPE);

        //GlobalData.logE("@@@ EventsService.onHandleIntent","isRestart="+isRestart);
        GlobalData.logE("@@@ EventsService.onHandleIntent","forDelayAlarm="+forDelayAlarm);

        // get running events count
        List<EventTimeline> _etl = dataWrapper.getEventTimelineList();
        int runningEventCount0 = _etl.size();

        // no refresh notification and widgets
        ActivateProfileHelper.lockRefresh = true;

        //BluetoothScanAlarmBroadcastReceiver.getBoundedDevicesList(context);
        //BluetoothScanAlarmBroadcastReceiver.getScanResults(context);

        Profile mergedProfile = dataWrapper.getNoinitializedProfile("", "", 0);

        //Profile activatedProfile0 = null;

        if (isRestart)
        {
            GlobalData.logE("$$$ EventsService.onHandleIntent","restart events");

            // 1. pause events
            dataWrapper.sortEventsByPriorityDesc();
            for (Event _event : eventList)
            {
                GlobalData.logE("EventsService.onHandleIntent","state PAUSE");
                GlobalData.logE("EventsService.onHandleIntent","event._id="+_event._id);
                GlobalData.logE("EventsService.onHandleIntent","event.getStatus()="+_event.getStatus());

                if (_event.getStatus() != Event.ESTATUS_STOP)
                    // len pauzuj eventy
                    // pauzuj aj ked uz je zapauznuty
                    dataWrapper.doEventService(/*intent, */_event, true, true, interactive, forDelayAlarm, true, mergedProfile);
            }
            // 2. start events
            dataWrapper.sortEventsByPriorityAsc();
            for (Event _event : eventList)
            {
                GlobalData.logE("EventsService.onHandleIntent","state RUNNING");
                GlobalData.logE("EventsService.onHandleIntent","event._id="+_event._id);
                GlobalData.logE("EventsService.onHandleIntent","event.getStatus()="+_event.getStatus());

                if (_event.getStatus() != Event.ESTATUS_STOP)
                    // len spustaj eventy
                    // spustaj len ak este nebezi
                    dataWrapper.doEventService(/*intent, */_event, false, false, interactive, forDelayAlarm, true, mergedProfile);
            }
        }
        else
        {
            GlobalData.logE("$$$ EventsService.onHandleIntent","NO restart events");

            //activatedProfile0 = dataWrapper.getActivatedProfileFromDB();

            //1. pause events
            dataWrapper.sortEventsByPriorityDesc();
            for (Event _event : eventList)
            {
                GlobalData.logE("EventsService.onHandleIntent","state PAUSE");
                GlobalData.logE("EventsService.onHandleIntent","event._id="+_event._id);
                GlobalData.logE("EventsService.onHandleIntent","event.getStatus()="+_event.getStatus());

                if (_event.getStatus() != Event.ESTATUS_STOP)
                    // len pauzuj eventy
                    // pauzuj len ak este nie je zapauznuty
                    dataWrapper.doEventService(/*intent, */_event, true, false, interactive, forDelayAlarm, false, mergedProfile);
            }
            //2. start events
            dataWrapper.sortEventsByPriorityAsc();
            for (Event _event : eventList)
            {
                GlobalData.logE("EventsService.onHandleIntent","state RUNNING");
                GlobalData.logE("EventsService.onHandleIntent","event._id="+_event._id);
                GlobalData.logE("EventsService.onHandleIntent","event.getStatus()="+_event.getStatus());

                if (_event.getStatus() != Event.ESTATUS_STOP)
                    // len spustaj eventy
                    // spustaj len ak este nebezi
                    dataWrapper.doEventService(/*intent, */_event, false, false, interactive, forDelayAlarm, false, mergedProfile);
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

            boolean backgroundProfileActivated = false;
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
                            if (mergedProfile == null) {
                                dataWrapper.activateProfileFromEvent(profileId, interactive, false, false, "", true);
                                backgroundProfileActivated = true;
                            } else
                                mergedProfile.mergeProfiles(profileId, dataWrapper);
                            GlobalData.logE("$$$ EventsService.onHandleIntent", "activated default profile");
                        }
                    }
                /*else
                if (activatedProfile == null)
                {
                    if (mergedProfile == null) {
                        dataWrapper.activateProfileFromEvent(0, interactive, "");
                        backgroundProfileActivated = true;
                    }
                    else
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
                        if (mergedProfile == null) {
                            dataWrapper.activateProfileFromEvent(profileId, interactive, false, false, "", true);
                            backgroundProfileActivated = true;
                        } else
                            mergedProfile.mergeProfiles(profileId, dataWrapper);
                        GlobalData.logE("$$$ EventsService.onHandleIntent", "not activated profile");
                    }
                }
            }
            ////////////////

            if (!backgroundProfileActivated) {
                // no background profile activated, refresh notification and widgets for activated profile

                String eventNotificationSound = "";

                if ((!isRestart) && (runningEventCountE > runningEventCount0)) {
                    // only when not restart events and running events is increased, play event notification sound

                    EventTimeline eventTimeline = eventTimelineList.get(runningEventCountE - 1);
                    Event event = dataWrapper.getEventById(eventTimeline._fkEvent);
                    if (event != null)
                        eventNotificationSound = event._notificationSound;
                }

                if (mergedProfile == null)
                    dataWrapper.updateNotificationAndWidgets(activatedProfile, eventNotificationSound);
                else {
                    if (mergedProfile._id != 0) {
                        // activate merged profile
                        GlobalData.logE("$$$ EventsService.onHandleIntent", "profileName=" + mergedProfile._name);
                        GlobalData.logE("$$$ EventsService.onHandleIntent", "profileId=" + mergedProfile._id);
                        dataWrapper.getDatabaseHandler().saveMergedProfile(mergedProfile);
                        dataWrapper.activateProfileFromEvent(mergedProfile._id, interactive, false, true, eventNotificationSound, false);
                    } else {
                    /*long prId0 = 0;
                    long prId = 0;
                    if (activatedProfile0 != null) prId0 = activatedProfile0._id;
                    if (activatedProfile != null) prId = activatedProfile._id;
                    if ((prId0 != prId) || (prId == 0))*/
                        dataWrapper.updateNotificationAndWidgets(activatedProfile, eventNotificationSound);
                    }
                }
            }

        }

        restartAtEndOfEvent = false;

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
        if (broadcastReceiverType.equals(EventDelayBroadcastReceiver.BROADCAST_RECEIVER_TYPE))
            eventType = DatabaseHandler.ETYPE_????;*/
        else
        if (broadcastReceiverType.equals(EventsCalendarBroadcastReceiver.BROADCAST_RECEIVER_TYPE))
            eventType = DatabaseHandler.ETYPE_CALENDAR;
        else
        if (broadcastReceiverType.equals(EventsTimeBroadcastReceiver.BROADCAST_RECEIVER_TYPE))
            eventType = DatabaseHandler.ETYPE_TIME;
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
        else
        if (broadcastReceiverType.equals(ScreenOnOffBroadcastReceiver.BROADCAST_RECEIVER_TYPE))
            eventType = DatabaseHandler.ETYPE_SCREEN;
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


        if (eventType > 0)
            return dataWrapper.getDatabaseHandler().getTypeEventsCount(eventType) > 0;
        else
            return true;
    }

    private void doEndService(Intent intent)
    {
        // completting wake
        if (broadcastReceiverType.equals(BatteryEventBroadcastReceiver.BROADCAST_RECEIVER_TYPE))
            BatteryEventBroadcastReceiver.completeWakefulIntent(intent);
        else
        if (broadcastReceiverType.equals(BluetoothScanBroadcastReceiver.BROADCAST_RECEIVER_TYPE))
            BluetoothScanBroadcastReceiver.completeWakefulIntent(intent);
        else
        if (broadcastReceiverType.equals(BluetoothLEScanBroadcastReceiver.BROADCAST_RECEIVER_TYPE))
            BluetoothLEScanBroadcastReceiver.completeWakefulIntent(intent);
        else
        if (broadcastReceiverType.equals(BluetoothScanBroadcastReceiver.BROADCAST_RECEIVER_TYPE))
            BluetoothScanBroadcastReceiver.completeWakefulIntent(intent);
        else
        if (broadcastReceiverType.equals(BluetoothStateChangedBroadcastReceiver.BROADCAST_RECEIVER_TYPE))
            BluetoothStateChangedBroadcastReceiver.completeWakefulIntent(intent);
        else
        if (broadcastReceiverType.equals(CalendarProviderChangedBroadcastReceiver.BROADCAST_RECEIVER_TYPE))
            CalendarProviderChangedBroadcastReceiver.completeWakefulIntent(intent);
        else
        if (broadcastReceiverType.equals(DockConnectionBroadcastReceiver.BROADCAST_RECEIVER_TYPE))
            DockConnectionBroadcastReceiver.completeWakefulIntent(intent);
        else
        if (broadcastReceiverType.equals(EventDelayBroadcastReceiver.BROADCAST_RECEIVER_TYPE))
            EventDelayBroadcastReceiver.completeWakefulIntent(intent);
        else
        if (broadcastReceiverType.equals(EventsCalendarBroadcastReceiver.BROADCAST_RECEIVER_TYPE))
            EventsCalendarBroadcastReceiver.completeWakefulIntent(intent);
        else
        if (broadcastReceiverType.equals(EventsTimeBroadcastReceiver.BROADCAST_RECEIVER_TYPE))
            EventsTimeBroadcastReceiver.completeWakefulIntent(intent);
        else
        if (broadcastReceiverType.equals(HeadsetConnectionBroadcastReceiver.BROADCAST_RECEIVER_TYPE))
            HeadsetConnectionBroadcastReceiver.completeWakefulIntent(intent);
        else
        if (broadcastReceiverType.equals(NotificationBroadcastReceiver.BROADCAST_RECEIVER_TYPE))
            NotificationBroadcastReceiver.completeWakefulIntent(intent);
        else
        if (broadcastReceiverType.equals(NotificationEventEndBroadcastReceiver.BROADCAST_RECEIVER_TYPE))
            NotificationEventEndBroadcastReceiver.completeWakefulIntent(intent);
        else
        if (broadcastReceiverType.equals(RestartEventsBroadcastReceiver.BROADCAST_RECEIVER_TYPE))
            RestartEventsBroadcastReceiver.completeWakefulIntent(intent);
        else
        if (broadcastReceiverType.equals(ScreenOnOffBroadcastReceiver.BROADCAST_RECEIVER_TYPE))
            ScreenOnOffBroadcastReceiver.completeWakefulIntent(intent);
        else
        if (broadcastReceiverType.equals(SearchCalendarEventsBroadcastReceiver.BROADCAST_RECEIVER_TYPE))
            SearchCalendarEventsBroadcastReceiver.completeWakefulIntent(intent);
        else
        if (broadcastReceiverType.equals(SMSBroadcastReceiver.BROADCAST_RECEIVER_TYPE))
            SMSBroadcastReceiver.completeWakefulIntent(intent);
        else
        if (broadcastReceiverType.equals(SMSEventEndBroadcastReceiver.BROADCAST_RECEIVER_TYPE))
            SMSEventEndBroadcastReceiver.completeWakefulIntent(intent);
        else
        if (broadcastReceiverType.equals(WifiConnectionBroadcastReceiver.BROADCAST_RECEIVER_TYPE))
            WifiConnectionBroadcastReceiver.completeWakefulIntent(intent);
        else
        if (broadcastReceiverType.equals(WifiScanBroadcastReceiver.BROADCAST_RECEIVER_TYPE))
            WifiScanBroadcastReceiver.completeWakefulIntent(intent);
        else
        if (broadcastReceiverType.equals(WifiStateChangedBroadcastReceiver.BROADCAST_RECEIVER_TYPE))
            WifiStateChangedBroadcastReceiver.completeWakefulIntent(intent);
        else
        if (broadcastReceiverType.equals(DeviceIdleModeBroadcastReceiver.BROADCAST_RECEIVER_TYPE))
            DeviceIdleModeBroadcastReceiver.completeWakefulIntent(intent);
        else
        if (broadcastReceiverType.equals(PowerSaveModeBroadcastReceiver.BROADCAST_RECEIVER_TYPE))
            PowerSaveModeBroadcastReceiver.completeWakefulIntent(intent);


        // this broadcast not starts service with wakefull method
        //if (broadcastReceiverType.equals(PhoneCallBroadcastReceiver.BROADCAST_RECEIVER_TYPE))
        //    PhoneCallBroadcastReceiver.completeWakefulIntent(intent);
    }

}
