package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.sip.SipAudioCall;
import android.net.sip.SipProfile;

public class SIPCallBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        SipAudioCall incomingCall = null;

        try {

            SipAudioCall.Listener listener = new SipAudioCall.Listener() {

                @Override
                public void onRinging(SipAudioCall call, SipProfile caller) {
                    GlobalData.logE("SIPCallBroadcastReceiver","onRinging");
                }
                @Override
                public void onCallBusy(SipAudioCall call) {
                    GlobalData.logE("SIPCallBroadcastReceiver","onCallBusy");
                }
                @Override
                public void onCallEnded(SipAudioCall call) {
                    GlobalData.logE("SIPCallBroadcastReceiver","onCallEnded");
                }
                @Override
                public void onCallEstablished(SipAudioCall call) {
                    GlobalData.logE("SIPCallBroadcastReceiver","onCallEstablished");
                }
                @Override
                public void onCallHeld(SipAudioCall call) {
                    GlobalData.logE("SIPCallBroadcastReceiver","onCallHeld");
                }
                @Override
                public void onCalling(SipAudioCall call) {
                    GlobalData.logE("SIPCallBroadcastReceiver","onCalling");
                }
                @Override
                public void onChanged(SipAudioCall call) {
                    GlobalData.logE("SIPCallBroadcastReceiver","onChanged");
                }
                @Override
                public void onError(SipAudioCall call, int errorCode, String errorMessage) {
                    GlobalData.logE("SIPCallBroadcastReceiver","onError");
                }
                @Override
                public void onReadyToCall(SipAudioCall call) {
                    GlobalData.logE("SIPCallBroadcastReceiver","onReadyToCall");
                }
                @Override
                public void onRingingBack(SipAudioCall call) {
                    GlobalData.logE("SIPCallBroadcastReceiver","onRingingBack");
                }

            };

        } catch (Exception ignored) {
        }

    }
}
