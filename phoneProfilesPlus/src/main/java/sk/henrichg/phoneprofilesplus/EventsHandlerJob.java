package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;

class EventsHandlerJob extends Job {

    static final String JOB_TAG  = "EventsHandlerJob";

    private static final String EXTRA_SENSOR_TYPE = "sensor_type";
    private static final String EXTRA_EVENT_NOTIFICATION_POSTED_REMOVED = "event_notification_posted_removed";
    private static final String EXTRA_EVENT_SMS_PHONE_NUMBER = "event_sms_phone_number";
    private static final String EXTRA_EVENT_SMS_DATE = "event_sms_date";
    private static final String EXTRA_EVENT_NFC_DATE = "event_nfc_date";
    private static final String EXTRA_EVENT_NFC_TAG_NAME = "event_nfc_tag_name";
    private static final String EXTRA_EVENT_RADIO_SWITCH_TYPE = "event_radio_switch_type";
    private static final String EXTRA_EVENT_RADIO_SWITCH_STATE = "event_radio_switch_state";

    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        final Context appContext = getContext().getApplicationContext();
        CallsCounter.logCounter(appContext, "EventsHandlerJob.onRunJob", "EventsHandlerJob_onRunJob");

        if (!PPApplication.getApplicationStarted(appContext, true))
            // application is not started
            return Result.SUCCESS;

        Bundle bundle = params.getTransientExtras();

        String sensorType = bundle.getString(EXTRA_SENSOR_TYPE);
        PPApplication.logE("#### EventsHandlerJob.onRunJob", "sensorType=" + sensorType);
        CallsCounter.logCounterNoInc(appContext, "EventsHandlerJob.onRunJob->sensorType="+sensorType, "EventsHandlerJob_onRunJob");

        EventsHandler eventsHandler = new EventsHandler(appContext);
        eventsHandler.setEventNotificationParameters(bundle.getString(EXTRA_EVENT_NOTIFICATION_POSTED_REMOVED));
        eventsHandler.setEventSMSParameters(bundle.getString(EXTRA_EVENT_SMS_PHONE_NUMBER), bundle.getLong(EXTRA_EVENT_SMS_DATE, 0));
        eventsHandler.setEventNFCParameters(bundle.getString(EXTRA_EVENT_NFC_TAG_NAME), bundle.getLong(EXTRA_EVENT_NFC_DATE, 0));
        eventsHandler.setEventRadioSwitchParameters(bundle.getInt(EXTRA_EVENT_RADIO_SWITCH_TYPE, 0), bundle.getBoolean(EXTRA_EVENT_RADIO_SWITCH_STATE, false));
        eventsHandler.handleEvents(sensorType, bundle.getBoolean(DataWrapper.EXTRA_INTERACTIVE, false));
        return Result.SUCCESS;
    }

    static void startForSensor(String sensorType) {
        JobRequest.Builder jobBuilder = new JobRequest.Builder(JOB_TAG);

        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_SENSOR_TYPE, sensorType);

        jobBuilder
                .setUpdateCurrent(false) // don't update current, it would cancel this currently running job
                .setTransientExtras(bundle)
                .startNow()
                .build()
                .schedule();
    }

    static void startForRestartEvents(boolean interactive) {
        JobRequest.Builder jobBuilder = new JobRequest.Builder(JOB_TAG);

        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_SENSOR_TYPE, EventsHandler.SENSOR_TYPE_RESTART_EVENTS);
        bundle.getBoolean(DataWrapper.EXTRA_INTERACTIVE, interactive);

        jobBuilder
                .setUpdateCurrent(false) // don't update current, it would cancel this currently running job
                .setTransientExtras(bundle)
                .startNow()
                .build()
                .schedule();
    }

    static void startForRadioSwitchSensor(int switchType, boolean state) {
        JobRequest.Builder jobBuilder = new JobRequest.Builder(JOB_TAG);

        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_SENSOR_TYPE, EventsHandler.SENSOR_TYPE_RADIO_SWITCH);
        bundle.putInt(EXTRA_EVENT_RADIO_SWITCH_TYPE, switchType);
        bundle.putBoolean(EXTRA_EVENT_RADIO_SWITCH_STATE, state);

        jobBuilder
                .setUpdateCurrent(false) // don't update current, it would cancel this currently running job
                .setTransientExtras(bundle)
                .startNow()
                .build()
                .schedule();
    }

    static void startForNFCTagSensor(String tagName, long date) {
        JobRequest.Builder jobBuilder = new JobRequest.Builder(JOB_TAG);

        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_SENSOR_TYPE, EventsHandler.SENSOR_TYPE_NFC_TAG);
        bundle.putString(EXTRA_EVENT_NFC_TAG_NAME, tagName);
        bundle.putLong(EXTRA_EVENT_NFC_DATE, date);

        jobBuilder
                .setUpdateCurrent(false) // don't update current, it would cancel this currently running job
                .setTransientExtras(bundle)
                .startNow()
                .build()
                .schedule();
    }

    static void startForNotificationSensor(String postedRemoved) {
        JobRequest.Builder jobBuilder = new JobRequest.Builder(JOB_TAG);

        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_SENSOR_TYPE, EventsHandler.SENSOR_TYPE_NOTIFICATION);
        bundle.putString(EXTRA_EVENT_NOTIFICATION_POSTED_REMOVED, postedRemoved);

        jobBuilder
                .setUpdateCurrent(false) // don't update current, it would cancel this currently running job
                .setTransientExtras(bundle)
                .startNow()
                .build()
                .schedule();
    }

    static void startForSMSSensor(String phoneNumber, long date) {
        JobRequest.Builder jobBuilder = new JobRequest.Builder(JOB_TAG);

        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_SENSOR_TYPE, EventsHandler.SENSOR_TYPE_SMS);
        bundle.putString(EXTRA_EVENT_SMS_PHONE_NUMBER, phoneNumber);
        bundle.putLong(EXTRA_EVENT_SMS_DATE, date);

        jobBuilder
                .setUpdateCurrent(false) // don't update current, it would cancel this currently running job
                .setTransientExtras(bundle)
                .startNow()
                .build()
                .schedule();
    }
}
