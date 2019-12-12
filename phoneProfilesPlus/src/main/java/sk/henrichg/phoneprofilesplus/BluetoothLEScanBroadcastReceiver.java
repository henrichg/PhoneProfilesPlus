package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.PowerManager;

import java.util.concurrent.TimeUnit;

import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import static android.content.Context.POWER_SERVICE;

public class BluetoothLEScanBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        PPApplication.logE("##### BluetoothLEScanBroadcastReceiver.onReceive", "xxx");

        CallsCounter.logCounter(context, "BluetoothLEScanBroadcastReceiver.onReceive", "BluetoothLEScanBroadcastReceiver_onReceive");

        final Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(appContext, true))
            // application is not started
            return;

        final int forceOneScan = WifiBluetoothScanner.getForceOneLEBluetoothScan(appContext);

        if (Event.getGlobalEventsRunning(appContext) || (forceOneScan == WifiBluetoothScanner.FORCE_ONE_SCAN_FROM_PREF_DIALOG))
        {
            PPApplication.startHandlerThread("BluetoothLEScanBroadcastReceiver.onReceive.1");
            final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":BluetoothLEScanBroadcastReceiver_onReceive");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        PPApplication.logE("PPApplication.startHandlerThread", "START run - from=BluetoothLEScanBroadcastReceiver.onReceive.1");

                        //boolean scanStarted = (BluetoothScanWorker.getWaitForLEResults(appContext));

                        //if (scanStarted) {
                            PPApplication.logE("@@@ BluetoothLEScanBroadcastReceiver.onReceive", "xxx");


                            BluetoothScanWorker.fillBoundedDevicesList(appContext);

                            BluetoothScanWorker.setWaitForLEResults(appContext, false);

                            WifiBluetoothScanner.setForceOneLEBluetoothScan(appContext, WifiBluetoothScanner.FORCE_ONE_SCAN_DISABLED);

                            if (forceOneScan != WifiBluetoothScanner.FORCE_ONE_SCAN_FROM_PREF_DIALOG)// not start service for force scan
                            {
                                Data workData = new Data.Builder()
                                        .putString(PhoneProfilesService.EXTRA_DELAYED_WORK, DelayedWorksWorker.DELAYED_WORK_HANDLE_EVENTS)
                                        .putString(PhoneProfilesService.EXTRA_SENSOR_TYPE, EventsHandler.SENSOR_TYPE_BLUETOOTH_SCANNER)
                                        .build();

                                OneTimeWorkRequest afterFirstStartWorker =
                                        new OneTimeWorkRequest.Builder(DelayedWorksWorker.class)
                                                .setInputData(workData)
                                                .setInitialDelay(5, TimeUnit.SECONDS)
                                                .build();
                                try {
                                    WorkManager workManager = WorkManager.getInstance(context);
                                    workManager.enqueueUniqueWork("handleEventsBluetoothLEScannerWork", ExistingWorkPolicy.REPLACE, afterFirstStartWorker);
                                } catch (Exception ignored) {}

                                /*PPApplication.startHandlerThread("BluetoothLEScanBroadcastReceiver.onReceive");
                                final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                                        PowerManager.WakeLock wakeLock = null;
                                        try {
                                            if (powerManager != null) {
                                                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":BluetoothLEScanBroadcastReceiver_onReceive");
                                                wakeLock.acquire(10 * 60 * 1000);
                                            }

                                            // start events handler
                                            EventsHandler eventsHandler = new EventsHandler(appContext);
                                            eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_BLUETOOTH_SCANNER);
                                        } finally {
                                            if ((wakeLock != null) && wakeLock.isHeld()) {
                                                try {
                                                    wakeLock.release();
                                                } catch (Exception ignored) {}
                                            }
                                        }
                                    }
                                }, 5000);*/
                                //PostDelayedBroadcastReceiver.setAlarmForHandleEvents(EventsHandler.SENSOR_TYPE_BLUETOOTH_SCANNER, 5, appContext);
                            }
                        //}

                        PPApplication.logE("PPApplication.startHandlerThread", "END run - from=BluetoothLEScanBroadcastReceiver.onReceive.1");
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
    }

}
