package sk.henrichg.phoneprofilesplus;

import android.app.assist.AssistContent;
import android.app.assist.AssistStructure;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.service.voice.VoiceInteractionSession;
import androidx.annotation.RequiresApi;
//import com.arlosoft.macrodroid.macro.C6023h;
//import com.arlosoft.macrodroid.macro.Macro;
//import com.arlosoft.macrodroid.triggers.HomeButtonLongPressTrigger;
//import com.arlosoft.macrodroid.triggers.Trigger;
//import java.util.ArrayList;
//import java.util.Iterator;
//import kotlin.jvm.internal.C0330j;

@RequiresApi(23)
/* renamed from: com.arlosoft.macrodroid.voiceservice.a */
/* compiled from: MacroDroidVoiceSession.kt */
public final class PPPVoiceInteractionSession extends VoiceInteractionSession {
    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public PPPVoiceInteractionSession(Context context) {
        super(context);
        //C0330j.m960e(context, "context");
    }

    public void onHandleAssist(Bundle bundle, AssistStructure assistStructure, AssistContent assistContent) {
        super.onHandleAssist(bundle, assistStructure, assistContent);
        /*ArrayList arrayList = new ArrayList();
        for (Macro next : C6023h.m25685n().mo17464l()) {
            Iterator<Trigger> it = next.mo17345I().iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                Trigger next2 = it.next();
                if ((next2 instanceof HomeButtonLongPressTrigger) && next2.mo19205p2()) {
                    next.mo17366U0(next2);
                    if (next.mo17386g(next.mo17340F())) {
                        arrayList.add(next);
                    }
                }
            }
        }
        Iterator it2 = arrayList.iterator();
        while (it2.hasNext()) {
            Macro macro = (Macro) it2.next();
            macro.mo17353M(macro.mo17340F());
        }*/
    }

    public void onPrepareShow(Bundle bundle, int i) {
        //C0330j.m960e(bundle, "bundle");
        super.onPrepareShow(bundle, i);
        String string = bundle.getString("ACTION");
        if (string == null) {
            string = "";
        }
        if (string.equals("android.settings.VOICE_CONTROL_AIRPLANE_MODE")) {
            Intent intent = new Intent("android.settings.VOICE_CONTROL_AIRPLANE_MODE");
            intent.putExtra("airplane_mode_enabled", bundle.getBoolean("airplane_mode_enabled"));
            startVoiceActivity(intent);
        }
        if (string.equals("android.settings.VOICE_CONTROL_DO_NOT_DISTURB_MODE")) {
            Intent intent2 = new Intent("android.settings.VOICE_CONTROL_DO_NOT_DISTURB_MODE");
            intent2.putExtra("android.settings.extra.do_not_disturb_mode_enabled", bundle.getBoolean("android.settings.extra.do_not_disturb_mode_enabled"));
            intent2.putExtra("android.settings.extra.do_not_disturb_mode_minutes", bundle.getInt("android.settings.extra.do_not_disturb_mode_minutes"));
            startVoiceActivity(intent2);
        }
        if (string.equals("android.settings.VOICE_CONTROL_BATTERY_SAVER_MODE")) {
            Intent intent3 = new Intent("android.settings.VOICE_CONTROL_BATTERY_SAVER_MODE");
            intent3.putExtra("android.settings.extra.battery_saver_mode_enabled", bundle.getBoolean("android.settings.extra.battery_saver_mode_enabled"));
            startVoiceActivity(intent3);
        }
        if (Build.VERSION.SDK_INT >= 26) {
            setUiEnabled(false);
        }
    }
}
