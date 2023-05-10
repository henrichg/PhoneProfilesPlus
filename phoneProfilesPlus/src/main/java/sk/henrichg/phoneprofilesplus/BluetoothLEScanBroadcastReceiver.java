package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BluetoothLEScanBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
//        PPApplicationStatic.logE("[IN_BROADCAST] BluetoothLEScanBroadcastReceiver.onReceive", "xxx");

        final Context appContext = context.getApplicationContext();

        if (!PPApplicationStatic.getApplicationStarted(true, true))
            // application is not started
            return;

        BluetoothScanWorker.fillBoundedDevicesList(appContext);

        final int forceOneScan = ApplicationPreferences.prefForceOneBluetoothLEScan;

        if (EventStatic.getGlobalEventsRunning(context) || (forceOneScan == BluetoothScanner.FORCE_ONE_SCAN_FROM_PREF_DIALOG))
        {
            //if (scanStarted) {


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
                    if (PPApplicationStatic.getApplicationStarted(true)) {
                        WorkManager workManager = PPApplication.getWorkManagerInstance();
                        if (workManager != null) {

//                            //if (PPApplicationStatic.logEnabled()) {
//                            ListenableFuture<List<WorkInfo>> statuses;
//                            statuses = workManager.getWorkInfosForUniqueWork(MainWorker.HANDLE_EVENTS_BLUETOOTH_LE_SCANNER_WORK_TAG);
//                            try {
//                                List<WorkInfo> workInfoList = statuses.get();
//                            } catch (Exception ignored) {
//                            }
//                            //}

                            //workManager.enqueue(worker);
//                            PPApplicationStatic.logE("[WORKER_CALL] BluetoothLEScanBroadcastReceiver.onReceive", "xxx");
                            workManager.enqueueUniqueWork(MainWorker.HANDLE_EVENTS_BLUETOOTH_LE_SCANNER_WORK_TAG, ExistingWorkPolicy.REPLACE, worker);
                        }
                    }
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                }
                */
            }
            //}

        }
    }

}
