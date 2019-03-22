package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.provider.Settings;

public class TimeChangedReceiver extends BroadcastReceiver {
    public TimeChangedReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if ((intent != null) && (intent.getAction() != null)) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_TIMEZONE_CHANGED) ||
                    action.equals(Intent.ACTION_TIME_CHANGED)) {
                PPApplication.logE("##### TimeChangedReceiver.onReceive", "xxx");
                CallsCounter.logCounter(context, "TimeChangedReceiver.onReceive", "TimeChangedReceiver_onReceive");

                final Context appContext = context.getApplicationContext();

                if (!PPApplication.getApplicationStarted(appContext, true))
                    return;

                boolean timeChanged = true;

                if (action.equals(Intent.ACTION_TIME_CHANGED)) {
                    timeChanged = false;
                    String isAutoTime = Settings.Global.getString(appContext.getContentResolver(), Settings.Global.AUTO_TIME);
                    PPApplication.logE("TimeChangedReceiver.onReceive", "isAutoTime="+isAutoTime);
                    if ((isAutoTime != null) && isAutoTime.equals("0")) {
                        timeChanged = true;
                    }
                }
                PPApplication.logE("TimeChangedReceiver.onReceive", "timeChanged="+timeChanged);

                if (timeChanged) {
                    PPApplication.startHandlerThread("TimeChangedReceiver.onReceive");
                    final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            PPApplication.logE("PPApplication.startHandlerThread", "START run - from=TimeChangedReceiver.onReceive");

                            if (/*(android.os.Build.VERSION.SDK_INT >= 21) &&*/
                                    ApplicationPreferences.applicationUseAlarmClock(appContext)) {
                                ProfileDurationAlarmBroadcastReceiver.removeAlarm(appContext);
                                Profile.setActivatedProfileForDuration(appContext, 0);
                            }

                            SearchCalendarEventsJob.scheduleJob(false, null, true);

                            DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false);
                            //dataWrapper.clearSensorsStartTime();
                            dataWrapper.restartEvents(false, true, false, false, false);

                            PPApplication.logE("PPApplication.startHandlerThread", "END run - from=TimeChangedReceiver.onReceive");
                        }
                    });
                }
            }
        }
    }
}
