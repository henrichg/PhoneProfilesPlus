package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;

import com.commonsware.cwac.wakeful.WakefulIntentService;

public class EventsHandlerService extends WakefulIntentService {

    static final String EXTRA_SENSOR_TYPE = "sensor_type";
    static final String EXTRA_EVENT_NOTIFICATION_POSTED_REMOVED = "event_notification_posted_removed";
    static final String EXTRA_EVENT_SMS_PHONE_NUMBER = "event_sms_phone_number";
    static final String EXTRA_EVENT_SMS_DATE = "event_sms_date";
    static final String EXTRA_EVENT_NFC_DATE = "event_nfc_date";
    static final String EXTRA_EVENT_NFC_TAG_NAME = "event_nfc_tag_name";
    static final String EXTRA_EVENT_RADIO_SWITCH_TYPE = "event_radio_switch_type";
    static final String EXTRA_EVENT_RADIO_SWITCH_STATE = "event_radio_switch_state";

    //public static ArrayList<Profile> mergedProfiles = null;
    //public static Profile oldActivatedProfile = null;

    //public static final String BROADCAST_RECEIVER_TYPE_NO_BROADCAST_RECEIVER = "noBroadcastReceiver";

    public EventsHandlerService() {
        super("EventsHandlerService");

        // if enabled is true, onStartCommand(Intent, int, int) will return START_REDELIVER_INTENT,
        // so if this process dies before onHandleIntent(Intent) returns, the process will be restarted
        // and the intent redelivered. If multiple Intents have been sent, only the most recent one
        // is guaranteed to be redelivered.
        // -- but restarted service has intent == null??
        setIntentRedelivery(true);
    }

    @Override
    protected void doWakefulWork(Intent intent) {
        CallsCounter.logCounter(getApplicationContext(), "EventsHandlerService.doWakefulWork", "EventsService_doWakefulWork");

        if (intent == null) {
            PPApplication.logE("#### EventsHandlerService.onHandleIntent", "intent=null");
            return;
        }

        Context context = getApplicationContext();

        if (!PPApplication.getApplicationStarted(context, true))
            // application is not started
            return;

        PPApplication.logE("#### EventsHandlerService.onHandleIntent", "-- start --------------------------------");

        String sensorType = intent.getStringExtra(EXTRA_SENSOR_TYPE);
        PPApplication.logE("#### EventsHandlerService.onHandleIntent", "sensorType=" + sensorType);
        CallsCounter.logCounterNoInc(getApplicationContext(), "EventsHandlerService.doWakefulWork->sensorType="+sensorType, "EventsService_doWakefulWork");

        EventsHandler eventsHandler = new EventsHandler(context);
        eventsHandler.setEventSMSParameters(intent.getStringExtra(EXTRA_EVENT_SMS_PHONE_NUMBER), intent.getLongExtra(EXTRA_EVENT_SMS_DATE, 0));
        eventsHandler.setEventNotificationParameters(intent.getStringExtra(EXTRA_EVENT_NOTIFICATION_POSTED_REMOVED));
        eventsHandler.setEventNFCParameters(intent.getStringExtra(EXTRA_EVENT_NFC_TAG_NAME), intent.getLongExtra(EXTRA_EVENT_NFC_DATE, 0));
        eventsHandler.setEventRadioSwitchParameters(intent.getIntExtra(EXTRA_EVENT_RADIO_SWITCH_TYPE, 0), intent.getBooleanExtra(EXTRA_EVENT_RADIO_SWITCH_STATE, false));
        eventsHandler.handleEvents(sensorType, intent.getBooleanExtra(DataWrapper.EXTRA_INTERACTIVE, false));

        PPApplication.logE("@@@ EventsHandlerService.onHandleIntent","-- end --------------------------------");

    }

}
