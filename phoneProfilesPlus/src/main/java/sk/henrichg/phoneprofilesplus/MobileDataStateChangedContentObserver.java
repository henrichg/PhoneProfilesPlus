package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.PowerManager;

import static android.content.Context.POWER_SERVICE;

class MobileDataStateChangedContentObserver extends ContentObserver {

    //public static boolean internalChange = false;

    private static boolean previousState = false;

    private final Context context;

    MobileDataStateChangedContentObserver(Context c, Handler handler) {
        super(handler);

        context=c;

        previousState = ActivateProfileHelper.isMobileData(context);
    }

    /*
    @Override
    public boolean deliverSelfNotifications() {
        return super.deliverSelfNotifications();
    }
    */

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);

        PPApplication.logE("##### MobileDataStateChangedContentObserver", "onChange");

        CallsCounter.logCounter(context, "MobileDataStateChangedContentObserver.onChange", "MobileDataStateChangedContentObserver_onChange");

        if (PPApplication.hasSystemFeature(context, PackageManager.FEATURE_TELEPHONY)) {
            boolean actualState = ActivateProfileHelper.isMobileData(context);
            if (previousState != actualState) {

                if (Event.getGlobalEventsRunning(context)) {
                    //EventsHandlerJob.startForSensor(context, EventsHandler.SENSOR_TYPE_RADIO_SWITCH);
                    final Context appContext = context.getApplicationContext();
                    PPApplication.startHandlerThread("MobileDataStateChangedContentObserver.onChange");
                    final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                            PowerManager.WakeLock wakeLock = null;
                            if (powerManager != null) {
                                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MobileDataStateChangedContentObserver.onChange");
                                wakeLock.acquire(10 * 60 * 1000);
                            }

                            EventsHandler eventsHandler = new EventsHandler(appContext);
                            eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_RADIO_SWITCH/*, false*/);

                            if ((wakeLock != null) && wakeLock.isHeld())
                                wakeLock.release();
                        }
                    });
                }

                previousState = actualState;
            }
        }
    }

}
