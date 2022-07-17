package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BluetoothLEScanBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
//        PPApplication.logE("[IN_BROADCAST] BluetoothLEScanBroadcastReceiver.onReceive", "xxx");

        final Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(true))
            // application is not started
            return;

        BluetoothScanWorker.fillBoundedDevicesList(appContext);

        final int forceOneScan = ApplicationPreferences.prefForceOneBluetoothLEScan;

        if (Event.getGlobalEventsRunning() || (forceOneScan == BluetoothScanner.FORCE_ONE_SCAN_FROM_PREF_DIALOG))
        {
            //if (scanStarted) {
            //PPApplication.logE("@@@ BluetoothLEScanBroadcastReceiver.onReceive", "xxx");


            BluetoothScanWorker.setWaitForLEResults(appContext, false);
            BluetoothScanner.setForceOneLEBluetoothScan(appContext, BluetoothScanner.FORCE_ONE_SCAN_DISABLED);

            if (forceOneScan != BluetoothScanner.FORCE_ONE_SCAN_FROM_PREF_DIALOG)// not start service for force scan
            {
                PPExecutors.handleEvents(appContext, EventsHandler.SENSOR_TYPE_BLUETOOTH_SCANNER, "SENSOR_TYPE_BLUETOOTH_SCANNER", 5);

                /*
                Data workData = new Data.Builder()
                        .putInt(PhoneProfilesService.EXTRA_SENSOR_TYPE, EventsHandler.SENSOR_TYPE_BLUETOOTH_SCANNER)
                        .build();

                OneTimeWorkRequest worker =
                        new OneTimeWorkRequest.Builder(MainWorker.class)
                                .addTag(MainWorker.HANDLE_EVENTS_BLUETOOTH_LE_SCANNER_WORK_TAG)
                                .setInputData(workData)
                                .setInitialDelay(5, TimeUnit.SECONDS)
                                //.keepResultsForAtLeast(PPApplication.WORK_PRUNE_DELAY_MINUTES, TimeUnit.MINUTES)
                                .build();
                try {
                    if (PPApplication.getApplicationStarted(true)) {
                        WorkManager workManager = PPApplication.getWorkManagerInstance();
                        if (workManager != null) {

//                            //if (PPApplication.logEnabled()) {
//                            ListenableFuture<List<WorkInfo>> statuses;
//                            statuses = workManager.getWorkInfosForUniqueWork(MainWorker.HANDLE_EVENTS_BLUETOOTH_LE_SCANNER_WORK_TAG);
//                            try {
//                                List<WorkInfo> workInfoList = statuses.get();
//                                PPApplication.logE("[TEST BATTERY] BluetoothLEScanBroadcastReceiver.onReceive", "for=" + MainWorker.HANDLE_EVENTS_BLUETOOTH_LE_SCANNER_WORK_TAG + " workInfoList.size()=" + workInfoList.size());
//                            } catch (Exception ignored) {
//                            }
//                            //}

                            //workManager.enqueue(worker);
//                            PPApplication.logE("[WORKER_CALL] BluetoothLEScanBroadcastReceiver.onReceive", "xxx");
                            workManager.enqueueUniqueWork(MainWorker.HANDLE_EVENTS_BLUETOOTH_LE_SCANNER_WORK_TAG, ExistingWorkPolicy.REPLACE, worker);
                        }
                    }
                } catch (Exception e) {
                    PPApplication.recordException(e);
                }
                */
            }
            //}

        }
    }

}
