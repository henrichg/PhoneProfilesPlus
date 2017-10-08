package sk.henrichg.phoneprofilesplus;

import android.content.Intent;

import com.commonsware.cwac.wakeful.WakefulIntentService;

public class PhoneCallBroadcastReceiver extends PhoneCallReceiver {

    public static final String EXTRA_SERVICE_PHONE_EVENT = "service_phone_event";
    public static final String EXTRA_SERVICE_PHONE_INCOMING = "service_phone_incoming";
    public static final String EXTRA_SERVICE_PHONE_NUMBER = "service_phone_number";

    public static final int SERVICE_PHONE_EVENT_START = 1;
    public static final int SERVICE_PHONE_EVENT_ANSWER = 2;
    public static final int SERVICE_PHONE_EVENT_END = 3;

    protected boolean onStartReceive()
    {
        PPApplication.logE("##### PhoneCallBroadcastReceiver.onReceive", "xxx");

        if (!PPApplication.getApplicationStarted(savedContext, false))
            return false;

        return true;
    }

    protected void onEndReceive()
    {
    }

    protected void onIncomingCallStarted(String number/*, Date start*/)
    {
        PhoneCallJob.start(SERVICE_PHONE_EVENT_START, true, number);
    }

    protected void onOutgoingCallStarted(String number/*, Date start*/)
    {
        PhoneCallJob.start(SERVICE_PHONE_EVENT_START, false, number);
    }
    
    protected void onIncomingCallAnswered(String number/*, Date start*/)
    {
        PhoneCallJob.start(SERVICE_PHONE_EVENT_ANSWER, true, number);
    }

    protected void onOutgoingCallAnswered(String number/*, Date start*/)
    {
        PhoneCallJob.start(SERVICE_PHONE_EVENT_ANSWER, false, number);
    }
    
    protected void onIncomingCallEnded(String number/*, Date start, Date end*/)
    {
        PhoneCallJob.start(SERVICE_PHONE_EVENT_END, true, number);
    }

    protected void onOutgoingCallEnded(String number/*, Date start, Date end*/)
    {
        PhoneCallJob.start(SERVICE_PHONE_EVENT_END, false, number);
    }

    protected void onMissedCall(String number/*, Date start*/)
    {
        PhoneCallJob.start(SERVICE_PHONE_EVENT_END, true, number);
    }

}
