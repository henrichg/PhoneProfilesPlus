package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
//import kotlin.Metadata;
//import kotlin.jvm.internal.C0330j;

//@Metadata(mo937bv = {1, 0, 3}, mo938d1 = {"\u0000\u001d\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003*\u0001\u0000\b\n\u0018\u00002\u00020\u0001J!\u0010\u0007\u001a\u00020\u00062\b\u0010\u0003\u001a\u0004\u0018\u00010\u00022\u0006\u0010\u0005\u001a\u00020\u0004H\u0016¢\u0006\u0004\b\u0007\u0010\b¨\u0006\t"}, mo939d2 = {"com/arlosoft/macrodroid/voiceservice/MacroDroidVoiceService$broadcastReceiver$1", "Landroid/content/BroadcastReceiver;", "Landroid/content/Context;", "context", "Landroid/content/Intent;", "intent", "Lkotlin/n;", "onReceive", "(Landroid/content/Context;Landroid/content/Intent;)V", "app_standardRelease"}, mo940k = 1, mo941mv = {1, 5, 1})
/* compiled from: MacroDroidVoiceService.kt */
public final class PPPVoiceServiceBroadcastReceiver extends BroadcastReceiver {

    /* renamed from: a */
    final PPPVoiceService voiceService;

    PPPVoiceServiceBroadcastReceiver(PPPVoiceService _voiceService) {
        this.voiceService = _voiceService;
    }

    public void onReceive(Context context, Intent intent) {
        //C0330j.m960e(intent, "intent");
        String action = intent.getAction();
        if (action.equals(PPPVoiceService.ACTION_ASSISTANT)) {
            voiceService.showSession(intent.getExtras(), 0);
        }
    }
}
