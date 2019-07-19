package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.PowerManager;

import static android.content.Context.POWER_SERVICE;

// Delete button (X) or "clear all" in notification
public class NewMobileCellsNotificationDeletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### NewMobileCellsNotificationDeletedReceiver.onReceive", "xxx");

        CallsCounter.logCounter(context, "NewMobileCellsNotificationDeletedReceiver.onReceive", "NewMobileCellsNotificationDeletedReceiver_onReceive");

        if (intent != null) {
            final int mobileCellId = intent.getIntExtra(PhoneStateScanner.EXTRA_MOBILE_CELL_ID, 0);
            if (mobileCellId != 0) {
                // delete cell from database

                final Context appContext = context.getApplicationContext();
                PPApplication.startHandlerThread("NewMobileCellsNotificationDeletedReceiver.onReceive");
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

                            PPApplication.logE("PPApplication.startHandlerThread", "START run - from=NewMobileCellsNotificationDeletedReceiver.onReceive");

                            DatabaseHandler db = DatabaseHandler.getInstance(appContext);
                            db.deleteMobileCell(mobileCellId);

                            PPApplication.logE("PPApplication.startHandlerThread", "END run - from=NewMobileCellsNotificationDeletedReceiver.onReceive");
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
