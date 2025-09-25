package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.PowerManager;

class MobileDataStateChangedContentObserver extends ContentObserver {

    //public static boolean internalChange = false;

    private static volatile boolean previousState = false;
    private static volatile boolean previousStateSIM1 = false;
    private static volatile boolean previousStateSIM2 = false;

    private final Context context;

    MobileDataStateChangedContentObserver(Context c, Handler handler) {
        super(handler);

        context=c.getApplicationContext();

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
        if (EventStatic.getGlobalEventsRunning(context)) {
            final Context appContext = context.getApplicationContext();
            Runnable runnable = () -> {

                synchronized (PPApplication.handleEventsMutex) {

                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WakelockTags.WAKELOCK_TAG_MobileDataStateChangedContentObserver_onChange);
                            wakeLock.acquire(10 * 60 * 1000);
                        }

//                PPApplicationStatic.logE("[EVENTS_HANDLER_CALL] MobileDataStateChangedContentObserver.onChange", "SENSOR_TYPE_RADIO_SWITCH");
                        EventsHandler eventsHandler = new EventsHandler(appContext);
                        eventsHandler.handleEvents(new int[]{EventsHandler.SENSOR_TYPE_RADIO_SWITCH});

                    } catch (Exception e) {
//                        PPApplicationStatic.logE("[EVENTS_HANDLER_CALL] MobileDataStateChangedContentObserver.onChange", Log.getStackTraceString(e));
                        PPApplicationStatic.recordException(e);
                    } finally {
                        if ((wakeLock != null) && wakeLock.isHeld()) {
                            try {
                                wakeLock.release();
                            } catch (Exception ignored) {
                            }
                        }
                    }

                }
            };
//            PPApplicationStatic.logE("[EXECUTOR_CALL] MobileDataStateChangedContentObserver.doOnChange", "xxx");
            PPApplicationStatic.createEventsHandlerExecutor();
            PPApplication.eventsHandlerExecutor.submit(runnable);
        }
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
//        PPApplicationStatic.logE("[IN_OBSERVER] MobileDataStateChangedContentObserver.onChange", "uri="+uri);
//        PPApplicationStatic.logE("[IN_OBSERVER] MobileDataStateChangedContentObserver.onChange", "current thread="+Thread.currentThread());

        if (PPApplication.HAS_FEATURE_TELEPHONY) {
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
