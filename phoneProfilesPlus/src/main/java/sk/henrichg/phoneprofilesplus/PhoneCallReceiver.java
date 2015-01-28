package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;

import java.util.Date;

import android.support.v4.content.WakefulBroadcastReceiver;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public abstract class PhoneCallReceiver extends WakefulBroadcastReceiver {

	static TelephonyManager telephony;
    //The receiver will be recreated whenever android feels like it.  We need a static variable to remember data between instantiations
    static PhonecallStartEndDetector listener;
    //String outgoingSavedNumber;
    protected Context savedContext;


    @Override
    public void onReceive(Context context, Intent intent) {
        savedContext = context;
        
        if (onStartReceive())
        {
        	if (telephony == null)
        		telephony = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE); 
        	
	        if(listener == null){
	            listener = new PhonecallStartEndDetector();
	        }

        	//Log.e("PhoneCallReceiver.onReceive","action="+intent.getAction());
	        
	        //We listen to two intents.  The new outgoing call only tells us of an outgoing call.  We use it to get the number.
	        if (intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL")) {
            	//Log.e("PhoneCallReceiver.onReceive","outgoing call");
	            listener.setOutgoingNumber(intent.getExtras().getString("android.intent.extra.PHONE_NUMBER"));
	            return;
	        }
	
	        //The other intent tells us the phone state changed.  Here we set a listener to deal with it
	        telephony.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
        }
        onEndReceive();
    }
        

    //Derived classes should override these to respond to specific events of interest
    protected abstract boolean onStartReceive();
    protected abstract void onIncomingCallStarted(String number, Date start);
    protected abstract void onOutgoingCallStarted(String number, Date start);
    protected abstract void onOutgoingCallAnswered(String number, Date start);
    protected abstract void onIncomingCallAnswered(String number, Date start);
    protected abstract void onIncomingCallEnded(String number, Date start, Date end); 
    protected abstract void onOutgoingCallEnded(String number, Date start, Date end);
    protected abstract void onMissedCall(String number, Date start);
    protected abstract void onEndReceive();

    //Deals with actual events
    public class PhonecallStartEndDetector extends PhoneStateListener {
        int lastState = TelephonyManager.CALL_STATE_IDLE;
        Date callStartTime;
        boolean inCall;
        boolean isIncoming;
        String savedNumber;  //because the passed incoming is only valid in ringing

        public PhonecallStartEndDetector() {}

        //The outgoing number is only sent via a separate intent, so we need to store it out of band
        public void setOutgoingNumber(String number){
        	inCall = false;
            isIncoming = false;
            savedNumber = number;
            callStartTime = new Date();
            onOutgoingCallStarted(savedNumber, callStartTime);
        }

        //Incoming call-  goes from IDLE to RINGING when it rings, to OFFHOOK when it's answered, to IDLE when its hung up
        //Outgoing call-  goes from IDLE to OFFHOOK when it dials out, to IDLE when hung up
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            if(lastState == state){
                //No change, debounce extras
                return;
            }
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                	//Log.e("PhonecallStartEndDetector.onCallStateChanged","ringing");
                	inCall = false;
                    isIncoming = true;
                    callStartTime = new Date();
                    savedNumber = incomingNumber;
                    onIncomingCallStarted(incomingNumber, callStartTime);
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                	//Log.e("PhonecallStartEndDetector.onCallStateChanged","offhook");
                    //Transition of ringing->offhook are pickups of incoming calls.  Nothing donw on them
                    if(lastState != TelephonyManager.CALL_STATE_RINGING){
                    	//Log.e("PhonecallStartEndDetector.onCallStateChanged","isincoming=false");
                    	inCall = true;
                        isIncoming = false;
                        callStartTime = new Date();
                        onOutgoingCallAnswered(savedNumber, callStartTime);                      
                    }
                    else
                    {
                    	//Log.e("PhonecallStartEndDetector.onCallStateChanged","isincoming=true");
                    	inCall = true;
                        isIncoming = true;
                        callStartTime = new Date();
                    	onIncomingCallAnswered(savedNumber, callStartTime);
                    }
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                	//Log.e("PhonecallStartEndDetector.onCallStateChanged","idle");
                    //Went to idle-  this is the end of a call.  What type depends on previous state(s)
                    if(!inCall){
                        //Ring but no pickup-  a miss
                    	//Log.e("PhonecallStartEndDetector.onCallStateChanged","missed call");
                        onMissedCall(savedNumber, callStartTime);
                    }
                    else 
                    {
	                    if(isIncoming){
	                    	//Log.e("PhonecallStartEndDetector.onCallStateChanged","isincoming=true");
	                        onIncomingCallEnded(savedNumber, callStartTime, new Date());                        
	                    }
	                    else{
	                    	//Log.e("PhonecallStartEndDetector.onCallStateChanged","isincoming=false");
	                        onOutgoingCallEnded(savedNumber, callStartTime, new Date());                                                
	                    }
                    	inCall = false;
                    }
                    break;
            }
            lastState = state;

	        telephony.listen(listener, PhoneStateListener.LISTEN_NONE);
            
        }

    }

}
