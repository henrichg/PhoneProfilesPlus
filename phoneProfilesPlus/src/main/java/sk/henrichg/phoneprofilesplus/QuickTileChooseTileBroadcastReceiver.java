package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.PowerManager;
import android.service.quicksettings.TileService;
import android.widget.Toast;

public class QuickTileChooseTileBroadcastReceiver extends BroadcastReceiver {

    static final String EXTRA_QUICK_TILE_ID = "quick_tile_id";

    //@TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplication.logE("[IN_BROADCAST] QuickTileChooseTileBroadcastReceiver.onReceive", "xxx");

        //CallsCounter.logCounter(context, "QuickTileChooseTileBroadcastReceiver.onReceive", "QuickTileChooseTileBroadcastReceiver_onReceive");

        //final Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(true))
            // application is not started
            return;

        int tileId = intent.getIntExtra(EXTRA_QUICK_TILE_ID, -1);
//        PPApplication.logE("QuickTileChooseTileBroadcastReceiver.onReceive", "tileId="+tileId);
        if (tileId == -1)
            return;

        PPApplication.quickTileProfileId[tileId] = intent.getLongExtra(PPApplication.EXTRA_PROFILE_ID, -1);
//        PPApplication.logE("[IN_BROADCAST] PPTileService.chooseTileBroadcastReceiver", "profileId="+profileId);

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

        PPApplication.startHandlerThreadBroadcast(/*"AlarmClockBroadcastReceiver.onReceive"*/);
        final Handler __handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
        __handler.post(new PPApplication.PPHandlerThreadRunnable(context.getApplicationContext()) {
            @Override
            public void run() {
//                    PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=PPTileService.chooseTileBroadcastReceiver.onReceive");

                Context appContext= appContextWeakRef.get();

                if (appContext != null) {
                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":PTileService_chooseTileBroadcastReceiver_onReceive");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        String toast = context.getString(R.string.tile_chooser_tile_changed_toast);
                        if (PPApplication.quickTileProfileId[tileId] == Profile.RESTART_EVENTS_PROFILE_ID)
                            toast = toast + " " + context.getString(R.string.menu_restart_events);
                        else {
                            DataWrapper dataWrapper = new DataWrapper(context.getApplicationContext(), false, 0, false, 0, 0f);
                            Profile profile = dataWrapper.getProfileById(PPApplication.quickTileProfileId[tileId], false, false, false);
                            toast = toast + " " + profile._name;
                        }
                        PPApplication.showToast(context.getApplicationContext(), toast, Toast.LENGTH_LONG);

                    } catch (Exception e) {
//                        PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", Log.getStackTraceString(e));
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
            }
        });

    }
}
