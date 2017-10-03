package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;

import com.commonsware.cwac.wakeful.WakefulIntentService;

public class WifiAPStateChangeService extends WakefulIntentService {

    public WifiAPStateChangeService() {
        super("WifiAPStateChangeService");
    }

    @Override
    protected void doWakefulWork(Intent intent) {
        CallsCounter.logCounter(getApplicationContext(), "WifiAPStateChangeService.doWakefulWork", "WifiAPStateChangeService_doWakefulWork");
        if (intent != null) {

            Context context = getApplicationContext();

            if (Event.getGlobalEventsRunning(context))
            {
                if (WifiApManager.isWifiAPEnabled(context)) {
                    // Wifi AP is enabled
                    PPApplication.logE("WifiAPStateChangeService.doWakefulWork","wifi AP enabled");
                    WifiScanJob.cancelJob();
                }
                else {
                    PPApplication.logE("WifiAPStateChangeService.doWakefulWork","wifi AP disabled");
                    // send broadcast for one wifi scan
                    if (PhoneProfilesService.instance != null)
                        PhoneProfilesService.instance.scheduleWifiJob(true, true, true, false, true);
                }
            }
        }
    }

}
