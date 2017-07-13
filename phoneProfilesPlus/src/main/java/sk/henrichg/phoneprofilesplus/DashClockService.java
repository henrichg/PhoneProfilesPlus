package sk.henrichg.phoneprofilesplus;

import android.app.IntentService;
import android.content.Intent;

import com.commonsware.cwac.wakeful.WakefulIntentService;

public class DashClockService extends WakefulIntentService {

    public DashClockService() {
        super("DashClockService");
    }

    @Override
    protected void doWakefulWork(Intent intent) {
        if (intent != null) {
            PhoneProfilesDashClockExtension dashClockExtension = PhoneProfilesDashClockExtension.getInstance();
            if (dashClockExtension != null)
            {
                //PPApplication.loadPreferences(context);
                dashClockExtension.updateExtension();
            }
        }
    }

}
