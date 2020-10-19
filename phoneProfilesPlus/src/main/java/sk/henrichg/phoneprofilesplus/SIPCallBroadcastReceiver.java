package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SIPCallBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("[IN_BROADCAST] SIPCallBroadcastReceiver.onReceive", "xxx");

        //CallsCounter.logCounter(context, "SIPCallBroadcastReceiver.onReceive", "SIPCallBroadcastReceiver_onReceive");

        /*
        SipAudioCall incomingCall = null;

        try {


            SipAudioCall.Listener listener = new SipAudioCall.Listener() {

                @Override
                public void onRinging(SipAudioCall call, SipProfile caller) {
                    PPApplication.logE("SIPCallBroadcastReceiver","onRinging");
                }
                @Override
                public void onCallBusy(SipAudioCall call) {
                    PPApplication.logE("SIPCallBroadcastReceiver","onCallBusy");
                }
                @Override
                public void onCallEnded(SipAudioCall call) {
                    PPApplication.logE("SIPCallBroadcastReceiver","onCallEnded");
                }
                @Override
                public void onCallEstablished(SipAudioCall call) {
                    PPApplication.logE("SIPCallBroadcastReceiver","onCallEstablished");
                }
                @Override
                public void onCallHeld(SipAudioCall call) {
                    PPApplication.logE("SIPCallBroadcastReceiver","onCallHeld");
                }
                @Override
                public void onCalling(SipAudioCall call) {
                    PPApplication.logE("SIPCallBroadcastReceiver","onCalling");
                }
                @Override
                public void onChanged(SipAudioCall call) {
                    PPApplication.logE("SIPCallBroadcastReceiver","onChanged");
                }
                @Override
                public void onError(SipAudioCall call, int errorCode, String errorMessage) {
                    PPApplication.logE("SIPCallBroadcastReceiver","onError");
                }
                @Override
                public void onReadyToCall(SipAudioCall call) {
                    PPApplication.logE("SIPCallBroadcastReceiver","onReadyToCall");
                }
                @Override
                public void onRingingBack(SipAudioCall call) {
                    PPApplication.logE("SIPCallBroadcastReceiver","onRingingBack");
                }

            };

        } catch (Exception ignored) {
        }
        */

    }
}
