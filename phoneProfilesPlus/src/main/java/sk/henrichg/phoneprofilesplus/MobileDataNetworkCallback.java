package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.telephony.SubscriptionManager;

@SuppressWarnings("WeakerAccess")
public class MobileDataNetworkCallback extends ConnectivityManager.NetworkCallback {

    private final Context context;

    static boolean connected = false;

    MobileDataNetworkCallback(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public void onLost(Network network) {
        //record wi-fi disconnect event
//        PPApplication.logE("[IN_LISTENER] ----------- MobileDataNetworkCallback.onLost", "xxx");
        connected = false;
        doConnection();
    }

    @Override
    public void onUnavailable() {
//        PPApplication.logE("[IN_LISTENER] ----------- MobileDataNetworkCallback.onUnavailable", "xxx");
        doConnection();
    }

    @Override
    public void onLosing(Network network, int maxMsToLive) {
//        PPApplication.logE("[IN_LISTENER] ----------- MobileDataNetworkCallback.onLosing", "xxx");
        doConnection();
    }

    @Override
    public void onAvailable(Network network) {
        //record wi-fi connect event
//        PPApplication.logE("[IN_LISTENER] ----------- MobileDataNetworkCallback.onAvailable", "xxx");
        connected = true;
        doConnection();
    }

    private void doConnection() {
//        PPApplication.logE("[TEST BATTERY] MobileDataNetworkCallback.doConnection", "xxx");
//        PPApplication.logE("[TEST BATTERY] MobileDataNetworkCallback.doConnection", "current thread="+Thread.currentThread());

        //CallsCounter.logCounter(context, "MobileDataNetworkCallback.doConnection", "MobileDataNetworkCallback_doConnection");

        //final Context appContext = getApplicationContext();

        if (!PPApplication.getApplicationStarted(true))
            // application is not started
            return;

        //PPApplication.logE("[TEST BATTERY] MobileDataNetworkCallback.doConnection", "connected or disconnected");

        if (Build.VERSION.SDK_INT >= 26) {
            // configured is PPApplication.handlerThreadBroadcast handler (see PhoneProfilesService.registerCallbacks()
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = null;
            try {
                if (powerManager != null) {
                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":MobileDataNetworkCallback_doConnection_1");
                    wakeLock.acquire(10 * 60 * 1000);
                }

                _doConnection(context);

//               PPApplication.logE("PPApplication.startHandlerThread", "END run - from=MobileDataNetworkCallback.doConnection");

            } catch (Exception e) {
//                PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", Log.getStackTraceString(e));
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
        else {
            final Context appContext = this.context;
            PPApplication.startHandlerThreadBroadcast();
            final Handler __handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
            //__handler.post(new PPApplication.PPHandlerThreadRunnable(
            //        appContext) {
            __handler.post(() -> {
//                PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=MobileDataNetworkCallback.doConnection");

                //Context appContext= appContextWeakRef.get();
                //if (appContext != null) {
                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":MobileDataNetworkCallback_doConnection_2");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        MobileDataNetworkCallback.this._doConnection(appContext);

//                    PPApplication.logE("PPApplication.startHandlerThread", "END run - from=MobileDataNetworkCallback.doConnection");

                    } catch (Exception e) {
//                    PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                        PPApplication.recordException(e);
                    } finally {
                        if ((wakeLock != null) && wakeLock.isHeld()) {
                            try {
                                wakeLock.release();
                            } catch (Exception ignored) {
                            }
                        }
                    }
                //}
            });
        }
    }

    private void _doConnection(Context appContext) {
        PPApplication.logE("$$$ MobileDataNetworkCallback._doConnection", "isConnected=" + connected);

        if (Event.getGlobalEventsRunning()) {
            //if ((info.getState() == NetworkInfo.State.CONNECTED) ||
            //        (info.getState() == NetworkInfo.State.DISCONNECTED)) {

                //PPApplication.logE("$$$ MobileDataNetworkCallback._doConnection", "wifi is not scanned");

                if (PhoneProfilesService.getInstance() != null) {

                    if (connected) {
                        int defaultSubscriptionId = SubscriptionManager.getDefaultDataSubscriptionId();
                        PPApplication.logE("$$$ MobileDataNetworkCallback._doConnection", "defaultSubscriptionId=" + defaultSubscriptionId);
                    }


                    // start events handler
                    //PPApplication.logE("****** EventsHandler.handleEvents", "START run - from=MobileDataNetworkCallback._doConnection");

//                                    PPApplication.logE("[EVENTS_HANDLER_CALL] MobileDataNetworkCallback._doConnection", "sensorType=SENSOR_TYPE_RADIO_SWITCH");
                    EventsHandler eventsHandler = new EventsHandler(appContext);
                    eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_RADIO_SWITCH);

                    //PPApplication.logE("****** EventsHandler.handleEvents", "END run - from=MobileDataNetworkCallback._doConnection");
                }
        }
    }

}
