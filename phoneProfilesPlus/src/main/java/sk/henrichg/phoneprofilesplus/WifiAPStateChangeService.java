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
        if (intent != null) {

            Context context = getApplicationContext();

            //PPApplication.loadPreferences(context);

            if (Event.getGlobalEventsRuning(context))
            {
                if (WifiApManager.isWifiAPEnabled(context)) {
                    // Wifi AP is enabled
                    PPApplication.logE("WifiAPStateChangeService.doWakefulWork","wifi AP enabled");
                    WifiScanJob.cancelJob();
                }
                else {
                    PPApplication.logE("WifiAPStateChangeService.doWakefulWork","wifi AP disabled");
                    // send broadcast for one wifi scan
                    DataWrapper dataWrapper = new DataWrapper(context, false, false, 0);
                    if (dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_WIFIINFRONT) > 0)
                        WifiScanJob.scheduleJob(context, true, false, true);
                    dataWrapper.invalidateDataWrapper();
                }

            }
        }
    }

}
