package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Icon;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.IconCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class PPTileService extends TileService {

    private long profileId = 0;

    public final BroadcastReceiver chooseTileBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent ) {
        profileId = intent.getLongExtra(PPApplication.EXTRA_PROFILE_ID, -1);
        PPApplication.logE("[IN_BROADCAST] PPTileService.chooseTileBroadcastReceiver", "profileId="+profileId);
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(chooseTileBroadcastReceiver);
        //getApplicationContext().unregisterReceiver(chooseTileBroadcastReceiver);

        // update tile
        PPTileService.this.updateTile();
        }
    };

    @Override
    public void onClick () {
        super.onClick();
        // Called when the user click the tile

        if (profileId != 0) {
            DataWrapper dataWrapper = new DataWrapper(getApplicationContext(), false, 0, false);
            Profile profile = dataWrapper.getProfileById(profileId, false, false, false);
            if (profile != null) {
                PPApplication.logE("PPTileService.onClick", "profile=" + profile._name);
                Intent intent = new Intent(getApplicationContext(), BackgroundActivateProfileActivity.class);
                intent.setAction(Intent.ACTION_MAIN);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_QUICK_TILE);
                intent.putExtra(PPApplication.EXTRA_PROFILE_ID, profileId);
                startActivityAndCollapse(intent);
            }
        } else {
            Log.e("PPTileService.onClick", "xxxx");
            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(chooseTileBroadcastReceiver,
                    new IntentFilter(PPApplication.PACKAGE_NAME + ".ChooseTileBroadcastReceiver"+getTileId()));
            //getApplicationContext().registerReceiver(chooseTileBroadcastReceiver,
            //        new IntentFilter(PPApplication.PACKAGE_NAME + ".ChooseTileBroadcastReceiver"+getTileId()));

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
        profileId = 0;
    }

    @Override
    public void onTileAdded () {
        super.onTileAdded();
        // Do something when the user add the Tile
    }

    @Override
    public void onStartListening () {
        super.onStartListening();
        // Called when the Tile becomes visible
    }

    @Override
    public void onStopListening () {
        super.onStopListening();
        // Called when the tile is no longer visible
    }

    public void onDestroy () {
        super.onDestroy();
        Log.e("PPTileService.onDestroy", "xxxx");
    }

    int getTileId() {
        return 0;
    }

    void updateTile() {
        if (profileId != 0) {
            DataWrapper dataWrapper = new DataWrapper(getApplicationContext(), false, 0, false);
            Profile profile = dataWrapper.getProfileById(profileId, true, false, false);

            Tile tile = getQsTile();
            tile.setLabel(profile._name);

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
            tile.setState(Tile.STATE_ACTIVE);
            tile.updateTile();
        }
    }

}
