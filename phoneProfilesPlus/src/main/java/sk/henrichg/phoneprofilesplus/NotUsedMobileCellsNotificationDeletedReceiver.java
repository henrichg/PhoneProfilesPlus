package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.PowerManager;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.POWER_SERVICE;

// Delete button (X) or "clear all" in notification
public class NotUsedMobileCellsNotificationDeletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### NotUsedMobileCellsNotificationDeletedReceiver.onReceive", "xxx");

        CallsCounter.logCounter(context, "NotUsedMobileCellsNotificationDeletedReceiver.onReceive", "NewMobileCellsNotificationDeletedReceiver_onReceive");

        if (intent != null) {
            final int mobileCellId = intent.getIntExtra(NotUsedMobileCellsDetectedActivity.EXTRA_MOBILE_CELL_ID, 0);
            if (mobileCellId != 0) {
                final Context appContext = context.getApplicationContext();
                PPApplication.startHandlerThread("NotUsedMobileCellsNotificationDeletedReceiver.onReceive");
                final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                        PowerManager.WakeLock wakeLock = null;
                        try {
                            if (powerManager != null) {
                                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PhoneStateScanner_onCellInfoChanged");
                                wakeLock.acquire(10 * 60 * 1000);
                            }

                            PPApplication.logE("PPApplication.startHandlerThread", "START run - from=NotUsedMobileCellsNotificationDeletedReceiver.onReceive");

                            PPApplication.logE("NotUsedMobileCellsNotificationDeletedReceiver.onReceive", "mobileCellId="+mobileCellId);

                            DatabaseHandler db = DatabaseHandler.getInstance(appContext);

                            List<MobileCellsData> localCellsList = new ArrayList<>();
                            db.addMobileCellsToList(localCellsList, mobileCellId);
                            if (!localCellsList.isEmpty()) {
                                PPApplication.logE("NotUsedMobileCellsNotificationDeletedReceiver.onReceive", "save mobile cell");
                                MobileCellsData cell = localCellsList.get(0);
                                cell.doNotDetect = true;
                                db.saveMobileCellsList(localCellsList, true, false);
                            }

                            PPApplication.logE("PPApplication.startHandlerThread", "END run - from=NotUsedMobileCellsNotificationDeletedReceiver.onReceive");
                        } finally {
                            if ((wakeLock != null) && wakeLock.isHeld()) {
                                try {
                                    wakeLock.release();
                                } catch (Exception ignored) {
                                }
                            }
                        }
                    }
                });
            }
        }

    }

}
