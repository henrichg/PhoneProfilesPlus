package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.PowerManager;

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

        //PPApplication.logE("##### MobileDataStateChangedContentObserver", "onChange");

        //CallsCounter.logCounter(context, "MobileDataStateChangedContentObserver.onChange", "MobileDataStateChangedContentObserver_onChange");

        if (PPApplication.hasSystemFeature(context, PackageManager.FEATURE_TELEPHONY)) {
            boolean actualState = ActivateProfileHelper.isMobileData(context);
            if (previousState != actualState) {

                if (Event.getGlobalEventsRunning(context)) {
                    final Context appContext = context.getApplicationContext();
                    PPApplication.startHandlerThread("MobileDataStateChangedContentObserver.onChange");
                    final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                            PowerManager.WakeLock wakeLock = null;
                            try {
                                if (powerManager != null) {
                                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":MobileDataStateChangedContentObserver_onChange");
                                    wakeLock.acquire(10 * 60 * 1000);
                                }

                                //PPApplication.logE("PPApplication.startHandlerThread", "START run - from=MobileDataStateChangedContentObserver.onChange");

                                EventsHandler eventsHandler = new EventsHandler(appContext);
                                eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_RADIO_SWITCH);

                                //PPApplication.logE("PPApplication.startHandlerThread", "END run - from=MobileDataStateChangedContentObserver.onChange");
                            } finally {
                                if ((wakeLock != null) && wakeLock.isHeld()) {
                                    try {
                                        wakeLock.release();
                                    } catch (Exception ignored) {}
                                }
                            }
                        }
                    });
                }

                previousState = actualState;
            }
        }
    }

}
