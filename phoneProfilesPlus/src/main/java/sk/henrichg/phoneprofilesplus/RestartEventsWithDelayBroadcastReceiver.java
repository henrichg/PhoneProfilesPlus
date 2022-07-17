package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class RestartEventsWithDelayBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplication.logE("[IN_BROADCAST] RestartEventsWithDelayBroadcastReceiver.onReceive", "xxx");

        String action = intent.getAction();
        if (action != null) {
//            PPApplication.logE("RestartEventsWithDelayBroadcastReceiver.onReceive", "action=" + action);

            final boolean alsoRescan = intent.getBooleanExtra(PhoneProfilesService.EXTRA_ALSO_RESCAN, false);
            final boolean unblockEventsRun = intent.getBooleanExtra(PhoneProfilesService.EXTRA_UNBLOCK_EVENTS_RUN, false);
            final int logType = intent.getIntExtra(PhoneProfilesService.EXTRA_LOG_TYPE, PPApplication.ALTYPE_UNDEFINED);

            PPExecutors.scheduleRestartEventsWithDelayExecutor(alsoRescan, unblockEventsRun, logType, context);
        }
    }

}
