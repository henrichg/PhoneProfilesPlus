package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.service.quicksettings.TileService;
import android.widget.Toast;

public class QuickTileChooseTileBroadcastReceiver extends BroadcastReceiver {

    static final String EXTRA_QUICK_TILE_ID = "quick_tile_id";

    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplicationStatic.logE("[IN_BROADCAST] QuickTileChooseTileBroadcastReceiver.onReceive", "xxx");

        if (!PPApplicationStatic.getApplicationStarted(true, true))
            // application is not started
            return;

        final int tileId = intent.getIntExtra(EXTRA_QUICK_TILE_ID, -1);
        if (tileId == -1)
            return;

        PPApplication.quickTileProfileId[tileId] = intent.getLongExtra(PPApplication.EXTRA_PROFILE_ID, -1);
//        PPApplicationStatic.logE("[IN_BROADCAST] PPTileService.chooseTileBroadcastReceiver", "profileId="+PPApplication.quickTileProfileId[tileId]);

        // update Tile and save profileId int SharedPreferences
        ApplicationPreferences.setQuickTileProfileId(context.getApplicationContext(), tileId, PPApplication.quickTileProfileId[tileId]);

        // restart tile - this invoke onStartListening()
        // require in manifest file for TileService this meta data:
        //     <meta-data android:name="android.service.quicksettings.ACTIVE_TILE"
        //         android:value="true" />
        switch (tileId) {
            case 1:
                if ((PPApplication.quickTileProfileId[1] != 0) && (PPApplication.quickTileProfileId[1] != -1))
                    TileService.requestListeningState(context, new ComponentName(context, PPTileService1.class));
                break;
            case 2:
                if ((PPApplication.quickTileProfileId[2] != 0) && (PPApplication.quickTileProfileId[2] != -1))
                    TileService.requestListeningState(context, new ComponentName(context, PPTileService2.class));
                break;
            case 3:
                if ((PPApplication.quickTileProfileId[3] != 0) && (PPApplication.quickTileProfileId[3] != -1))
                    TileService.requestListeningState(context, new ComponentName(context, PPTileService3.class));
                break;
            case 4:
                if ((PPApplication.quickTileProfileId[4] != 0) && (PPApplication.quickTileProfileId[4] != -1))
                    TileService.requestListeningState(context, new ComponentName(context, PPTileService4.class));
                break;
            case 5:
                if ((PPApplication.quickTileProfileId[5] != 0) && (PPApplication.quickTileProfileId[5] != -1))
                    TileService.requestListeningState(context, new ComponentName(context, PPTileService5.class));
                break;
        }

        final Context appContext = context.getApplicationContext();
        Runnable runnable = () -> {
//                    PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=PPTileService.chooseTileBroadcastReceiver.onReceive");

            //Context appContext= appContextWeakRef.get();

            //if (appContext != null) {
                PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                try {
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WakelockTags.WAKELOCK_TAG_PTileService_chooseTileBroadcastReceiver_onReceive);
                        wakeLock.acquire(10 * 60 * 1000);
                    }

                    DataWrapper dataWrapper = new DataWrapper(appContext, false, 0, false, 0, 0, 0f);

                    String toast = appContext.getString(R.string.tile_chooser_tile_changed_toast);
                    if (PPApplication.quickTileProfileId[tileId] == Profile.RESTART_EVENTS_PROFILE_ID)
                        toast = toast + " " + appContext.getString(R.string.menu_restart_events);
                    else {
                        String profileName = dataWrapper.getProfileName(PPApplication.quickTileProfileId[tileId]);
                        if (profileName != null)
                            toast = toast + " " + profileName;
                    }
                    PPApplication.showToast(appContext, toast, Toast.LENGTH_LONG);

                    DataWrapperStatic.setDynamicLauncherShortcuts(appContext);

                    dataWrapper.invalidateDataWrapper();
                } catch (Exception e) {
//                        PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                    PPApplicationStatic.recordException(e);
                } finally {
                    if ((wakeLock != null) && wakeLock.isHeld()) {
                        try {
                            wakeLock.release();
                        } catch (Exception ignored) {
                        }
                    }
                }
            //}
        };
        PPApplicationStatic.createDelayedGuiExecutor();
        PPApplication.delayedGuiExecutor.submit(runnable);

    }
}
