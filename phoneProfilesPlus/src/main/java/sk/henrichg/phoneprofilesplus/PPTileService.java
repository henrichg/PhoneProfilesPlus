package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Handler;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import java.lang.ref.WeakReference;

public class PPTileService extends TileService {

    @Override
    public void onClick () {
        super.onClick();

        // Called when the user click the tile

//        PPApplication.logE("PPTileService.onClick", "xxx");

        int tileId = getTileId();
        // get profileId from shaered preferences
        PPApplication.quickTileProfileId[tileId] = ApplicationPreferences.getQuickTileProfileId(getApplicationContext(), tileId);
        updateTile();


        boolean isOK = false;
        if ((PPApplication.quickTileProfileId[tileId] != 0) && (PPApplication.quickTileProfileId[tileId] != -1)) {
            Profile profile = null;
            if (PPApplication.quickTileProfileId[tileId] != Profile.RESTART_EVENTS_PROFILE_ID) {
                DataWrapper dataWrapper = new DataWrapper(getApplicationContext(), false, 0, false, 0, 0f);
                profile = dataWrapper.getProfileById(PPApplication.quickTileProfileId[tileId], false, false, false);
//                PPApplication.logE("PPTileService.onClick", "profile="+profile);
//                if (profile != null)
//                    PPApplication.logE("PPTileService.onClick", "profile=" + profile._name);
            }
            if ((PPApplication.quickTileProfileId[tileId] == Profile.RESTART_EVENTS_PROFILE_ID) || (profile != null)) {
                isOK = true;
                Intent intent = new Intent(getApplicationContext(), BackgroundActivateProfileActivity.class);
                intent.setAction(Intent.ACTION_MAIN);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_QUICK_TILE);
                intent.putExtra(PPApplication.EXTRA_PROFILE_ID, PPApplication.quickTileProfileId[tileId]);
                startActivityAndCollapse(intent);
            }
        }
        if (!isOK) {
            try {
                if (PPApplication.quickTileChooseTileBroadcastReceiver[tileId] != null) {
                    getApplicationContext().unregisterReceiver(PPApplication.quickTileChooseTileBroadcastReceiver[tileId]);
                    //LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(PPApplication.quickTileChooseTileBroadcastReceiver[tileId]);
                    PPApplication.quickTileChooseTileBroadcastReceiver[tileId] = null;
                }
            } catch (Exception ignored) {}
            if (PPApplication.quickTileChooseTileBroadcastReceiver[tileId] == null) {
                PPApplication.quickTileChooseTileBroadcastReceiver[tileId] = new QuickTileChooseTileBroadcastReceiver();
                getApplicationContext().registerReceiver(PPApplication.quickTileChooseTileBroadcastReceiver[tileId],
                        new IntentFilter(PPApplication.PACKAGE_NAME + ".ChooseTileBroadcastReceiver" + tileId));
                //LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(PPApplication.quickTileChooseTileBroadcastReceiver[tileId],
                //        new IntentFilter(PPApplication.PACKAGE_NAME + ".ChooseTileBroadcastReceiver"+tileId));
            }

            Intent intent = new Intent(getApplicationContext(), TileChooserActivity.class);
            intent.setAction(Intent.ACTION_MAIN);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_QUICK_TILE);
            intent.putExtra(TileChooserActivity.EXTRA_TILE_ID, getTileId());
            startActivityAndCollapse(intent);
        }
    }

    @Override
    public void onTileRemoved () {
        super.onTileRemoved();
        // Do something when the user removes the Tile

        // set it inactive when removed
        int tileId = getTileId();
        PPApplication.quickTileProfileId[tileId] = 0;
        ApplicationPreferences.setQuickTileProfileId(getApplicationContext(), tileId, PPApplication.quickTileProfileId[tileId]);
        updateTile();
    }

    @Override
    public void onTileAdded () {
        super.onTileAdded();
        // Do something when the user add the Tile

        // get profileId from SharedPreferences and update it
        int tileId = getTileId();
        PPApplication.quickTileProfileId[tileId] = ApplicationPreferences.getQuickTileProfileId(getApplicationContext(), tileId);
        updateTile();
    }

    @Override
    public void onStartListening () {
        super.onStartListening();
        // Called when the Tile becomes visible
//        PPApplication.logE("PPTileService.onStartListening", "getTileId()="+getTileId());

        int tileId = getTileId();
        try {
            if (PPApplication.quickTileChooseTileBroadcastReceiver[tileId] != null) {
                getApplicationContext().unregisterReceiver(PPApplication.quickTileChooseTileBroadcastReceiver[tileId]);
                //LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(PPApplication.quickTileChooseTileBroadcastReceiver[tileId]);
                PPApplication.quickTileChooseTileBroadcastReceiver[tileId] = null;
            }
        } catch (Exception ignored) {}
        if (PPApplication.quickTileChooseTileBroadcastReceiver[tileId] == null) {
            PPApplication.quickTileChooseTileBroadcastReceiver[tileId] = new QuickTileChooseTileBroadcastReceiver();
            getApplicationContext().registerReceiver(PPApplication.quickTileChooseTileBroadcastReceiver[tileId],
                    new IntentFilter(PPApplication.PACKAGE_NAME + ".ChooseTileBroadcastReceiver" + tileId));
            //LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(PPApplication.quickTileChooseTileBroadcastReceiver[tileId],
            //        new IntentFilter(PPApplication.PACKAGE_NAME + ".ChooseTileBroadcastReceiver"+tileId));
        }

        // get profileId of tile from SharedPreferences and update it
        PPApplication.quickTileProfileId[tileId] = ApplicationPreferences.getQuickTileProfileId(getApplicationContext(), tileId);
        updateTile();
    }

    /*
    @Override
    public void onStopListening () {
        super.onStopListening();
        // Called when the tile is no longer visible
    }
    */

    /*
    @Override
    public void onDestroy () {
        super.onDestroy();
//        PPApplication.logE("PPTileService.onDestroy", "getTileId()="+getTileId());
//        try {
//            getApplicationContext().unregisterReceiver(chooseTileBroadcastReceiver);
//            //LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(chooseTileBroadcastReceiver);
//        } catch (Exception ignored) {}
    }
    */


    int getTileId() {
        return 0;
    }

    void updateTile() {
        Tile tile = getQsTile();
        if (tile == null)
            return;

//        PPApplication.logE("PPTileService.updateTile", "profileId="+profileId);

        int tileId = getTileId();
        if ((PPApplication.quickTileProfileId[tileId] != 0) && (PPApplication.quickTileProfileId[tileId] != -1)) {
            final Handler __handler = new Handler(PPApplication.handlerThreadWidget.getLooper());
            __handler.post(new PPHandlerThreadRunnable(getApplicationContext(), tile) {
                @Override
                public void run() {
//                            PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThreadWidget", "START run - from=IconWidgetProvider.onReceive");

                    Context appContext= appContextWeakRef.get();
                    Tile tile = tileWeakRef.get();

                    if ((appContext != null) && (tile != null)) {

//                        PPApplication.logE("PPTileService.updateTile", "udate tile");

                        if (PPApplication.quickTileProfileId[tileId] == Profile.RESTART_EVENTS_PROFILE_ID) {
                            tile.setLabel(getString(R.string.menu_restart_events));
                            if (Build.VERSION.SDK_INT >= 29) {
                                tile.setSubtitle(null);
                            }
                            tile.setIcon(Icon.createWithResource(getApplicationContext(), R.drawable.ic_list_item_events_restart_color_filled));
                            tile.setState(Tile.STATE_INACTIVE);
                        }
                        else {
                            DataWrapper dataWrapper = new DataWrapper(getApplicationContext(), false, 0, false, 0, 0f);
                            Profile profile = dataWrapper.getProfileById(PPApplication.quickTileProfileId[tileId], true, false, false);
                            if (profile != null) {
                                tile.setLabel(profile._name);
                                if (Build.VERSION.SDK_INT >= 29) {
                                    if (profile._checked)
                                        tile.setSubtitle(getString(R.string.quick_tile_subtile_activated));
                                    else
                                        tile.setSubtitle(getString(R.string.quick_tile_subtile_not_activated));
                                }

                                if (profile.getIsIconResourceID()) {
                                    if (profile._iconBitmap != null)
                                        tile.setIcon(Icon.createWithBitmap(profile._iconBitmap));
                                    else {
                                        int res = Profile.getIconResource(profile.getIconIdentifier());
                                        tile.setIcon(Icon.createWithResource(getApplicationContext(), res));
                                    }
                                } else {
                                    tile.setIcon(Icon.createWithBitmap(profile._iconBitmap));
                                }

                                if (profile._checked)
                                    tile.setState(Tile.STATE_ACTIVE);
                                else
                                    tile.setState(Tile.STATE_INACTIVE);
                            }
                        }
                        tile.updateTile();

                        // save tile profileId into SharedPreferences
                    }
                }
            });
        } else {
            tile.setLabel(getString(R.string.quick_tile_icon_label));
            tile.setIcon(Icon.createWithResource(getApplicationContext(), R.drawable.ic_profile_default));
            tile.setState(Tile.STATE_INACTIVE);
            tile.updateTile();
        }
    }

    private static abstract class PPHandlerThreadRunnable implements Runnable {

        public final WeakReference<Context> appContextWeakRef;
        public final WeakReference<Tile> tileWeakRef;

        public PPHandlerThreadRunnable(Context appContext,
                                       Tile tile) {
            this.appContextWeakRef = new WeakReference<>(appContext);
            this.tileWeakRef = new WeakReference<>(tile);
        }

    }

}
