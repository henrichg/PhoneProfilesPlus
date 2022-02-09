package sk.henrichg.phoneprofilesplus;

import android.os.Bundle;
import android.service.voice.VoiceInteractionSessionService;
//import kotlin.Metadata;

//@Metadata(mo937bv = {1, 0, 3}, mo938d1 = {"\u0000\u0016\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\u0018\u00002\u00020\u0001B\u0007¢\u0006\u0004\b\u0007\u0010\bJ\u0019\u0010\u0005\u001a\u00020\u00042\b\u0010\u0003\u001a\u0004\u0018\u00010\u0002H\u0016¢\u0006\u0004\b\u0005\u0010\u0006¨\u0006\t"}, mo939d2 = {"Lcom/arlosoft/macrodroid/voiceservice/MacroDroidVoiceSessionService;", "Landroid/service/voice/VoiceInteractionSessionService;", "Landroid/os/Bundle;", "bundle", "Lcom/arlosoft/macrodroid/voiceservice/a;", "a", "(Landroid/os/Bundle;)Lcom/arlosoft/macrodroid/voiceservice/a;", "<init>", "()V", "app_standardRelease"}, mo940k = 1, mo941mv = {1, 5, 1})
/* compiled from: MacroDroidVoiceSessionService.kt */
public final class PPPVoiceSessionService extends VoiceInteractionSessionService {
    /* renamed from: a */
    public PPPVoiceInteractionSession onNewSession(Bundle bundle) {
        return new PPPVoiceInteractionSession(this);
    }
}
