package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.PowerManager;

import static android.content.Context.POWER_SERVICE;

public class BluetoothLEScanBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### BluetoothLEScanBroadcastReceiver.onReceive", "xxx");

        CallsCounter.logCounter(context, "BluetoothLEScanBroadcastReceiver.onReceive", "BluetoothLEScanBroadcastReceiver_onReceive");

        final Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(appContext, true))
            // application is not started
            return;

        //BluetoothJob.startForLEScanBroadcast(context.getApplicationContext());

        final int forceOneScan = Scanner.getForceOneLEBluetoothScan(appContext);

        if (Event.getGlobalEventsRunning(appContext) || (forceOneScan == Scanner.FORCE_ONE_SCAN_FROM_PREF_DIALOG))
        {

            boolean scanStarted = (BluetoothScanJob.getWaitForLEResults(appContext));

            if (scanStarted)
            {
                PPApplication.logE("@@@ BluetoothLEScanBroadcastReceiver.onReceive","xxx");

                final Handler handler = new Handler(appContext.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "BluetoothLEScanBroadcastReceiver.onReceive");
                        wakeLock.acquire();

                        BluetoothScanJob.fillBoundedDevicesList(appContext);

                        BluetoothScanJob.setWaitForLEResults(appContext, false);

                        Scanner.setForceOneLEBluetoothScan(appContext, Scanner.FORCE_ONE_SCAN_DISABLED);

                        if (forceOneScan != Scanner.FORCE_ONE_SCAN_FROM_PREF_DIALOG)// not start service for force scan
                        {
                            // start job
                            new Handler(appContext.getMainLooper()).postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                                    PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "BluetoothLEScanBroadcastReceiver.onReceive.Handler.postDelayed");
                                    wakeLock.acquire();

                                    //EventsHandlerJob.startForSensor(appContext, EventsHandler.SENSOR_TYPE_BLUETOOTH_SCANNER);
                                    // start events handler
                                    EventsHandler eventsHandler = new EventsHandler(appContext);
                                    eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_BLUETOOTH_SCANNER, false);

                                    wakeLock.release();
                                }
                            }, 5000);
                        }

                        wakeLock.release();
                    }
                });
            }
        }

    }

}
