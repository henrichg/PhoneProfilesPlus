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

        int tileId = intent.getIntExtra(EXTRA_QUICK_TILE_ID, -1);
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
                TileService.requestListeningState(context, new ComponentName(context, PPTileService1.class));
                break;
            case 2:
                TileService.requestListeningState(context, new ComponentName(context, PPTileService2.class));
                break;
            case 3:
                TileService.requestListeningState(context, new ComponentName(context, PPTileService3.class));
                break;
            case 4:
                TileService.requestListeningState(context, new ComponentName(context, PPTileService4.class));
                break;
            case 5:
                TileService.requestListeningState(context, new ComponentName(context, PPTileService5.class));
                break;
        }

        final Context appContext = context.getApplicationContext();
        //PPApplication.startHandlerThreadBroadcast(/*"AlarmClockBroadcastReceiver.onReceive"*/);
        //final Handler __handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
        //__handler.post(new PPApplication.PPHandlerThreadRunnable(context.getApplicationContext()) {
        //__handler.post(() -> {
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

                    DataWrapper dataWrapper = new DataWrapper(context.getApplicationContext(), false, 0, false, 0, 0, 0f);

                    String toast = context.getString(R.string.tile_chooser_tile_changed_toast);
                    if (PPApplication.quickTileProfileId[tileId] == Profile.RESTART_EVENTS_PROFILE_ID)
                        toast = toast + " " + context.getString(R.string.menu_restart_events);
                    else {
                        Profile profile = dataWrapper.getProfileById(PPApplication.quickTileProfileId[tileId], false, false, false);
                        toast = toast + " " + profile._name;
                    }
                    PPApplication.showToast(context.getApplicationContext(), toast, Toast.LENGTH_LONG);

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
        }; //);
        PPApplicationStatic.createDelayedGuiExecutor();
        PPApplication.delayedGuiExecutor.submit(runnable);

    }
}
