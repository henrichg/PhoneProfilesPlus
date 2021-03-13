package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;

class MobileDataStateChangedContentObserver extends ContentObserver {

    //public static boolean internalChange = false;

    private static boolean previousState = false;
    private static boolean previousStateSIM1 = false;
    private static boolean previousStateSIM2 = false;

    private final Context context;

    MobileDataStateChangedContentObserver(Context c, Handler handler) {
        super(handler);

        context=c;

        previousState = ActivateProfileHelper.isMobileData(context, 0);
        previousStateSIM1 = ActivateProfileHelper.isMobileData(context, 1);
        previousStateSIM2 = ActivateProfileHelper.isMobileData(context, 2);
    }

    /*
    @Override
    public boolean deliverSelfNotifications() {
        return super.deliverSelfNotifications();
    }
    */

    private void doOnChange(/*boolean selfChange, Uri uri*/) {
        if (Event.getGlobalEventsRunning()) {
            final Context appContext = context.getApplicationContext();

            PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = null;
            try {
                if (powerManager != null) {
                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":MobileDataStateChangedContentObserver_onChange");
                    wakeLock.acquire(10 * 60 * 1000);
                }

//                        PPApplication.logE("[EVENTS_HANDLER_CALL] MobileDataStateChangedContentObserver.onChange", "sensorType=SENSOR_TYPE_RADIO_SWITCH");
                EventsHandler eventsHandler = new EventsHandler(appContext);
                eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_RADIO_SWITCH);

            } catch (Exception e) {
//                        PPApplication.logE("[EVENTS_HANDLER_CALL] MobileDataStateChangedContentObserver.onChange", Log.getStackTraceString(e));
                PPApplication.recordException(e);
            } finally {
                if ((wakeLock != null) && wakeLock.isHeld()) {
                    try {
                        wakeLock.release();
                    } catch (Exception ignored) {
                    }
                }
            }
        }
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        PPApplication.logE("[IN_OBSERVER] MobileDataStateChangedContentObserver.onChange", "uri="+uri);
//        PPApplication.logE("[IN_OBSERVER] MobileDataStateChangedContentObserver.onChange", "current thread="+Thread.currentThread());

        //CallsCounter.logCounter(context, "MobileDataStateChangedContentObserver.onChange", "MobileDataStateChangedContentObserver_onChange");

        if (PPApplication.HAS_FEATURE_TELEPHONY) {
            if (Build.VERSION.SDK_INT >= 26) {
                boolean actualStateSIM1 = ActivateProfileHelper.isMobileData(context, 1);
                boolean actualStateSIM2 = ActivateProfileHelper.isMobileData(context, 2);
                if (previousStateSIM1 != actualStateSIM1) {
                    doOnChange(/*selfChange, uri*/);
                    previousStateSIM1 = actualStateSIM1;
                }
                if (previousStateSIM2 != actualStateSIM2) {
                    doOnChange(/*selfChange, uri*/);
                    previousStateSIM2 = actualStateSIM2;
                }
            }
            boolean actualState = ActivateProfileHelper.isMobileData(context, 0);
            if (previousState != actualState) {
                doOnChange(/*selfChange, uri*/);
                previousState = actualState;
            }
        }
    }

    @Override
    public void onChange(boolean selfChange) {
        onChange(selfChange, null);
    }

}
