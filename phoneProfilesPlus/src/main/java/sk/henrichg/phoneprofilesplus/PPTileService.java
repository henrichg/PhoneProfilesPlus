package sk.henrichg.phoneprofilesplus;

import android.content.Intent;
import android.service.quicksettings.TileService;

public class PPTileService extends TileService {

    private long profileId = 0;

    @Override
    public void onClick () {
        super.onClick();
        // Called when the user click the tile
        if (profileId != 0) {
            DataWrapper dataWrapper = new DataWrapper(getApplicationContext(), false, 0, false);
            Profile profile = dataWrapper.getProfileById(profileId, false, false, false);
            if (profile != null) {
//                 PPApplication.logE("PPTileService.onClick", "profile=" + profile._name);
                Intent intent = new Intent(getApplicationContext(), BackgroundActivateProfileActivity.class);
                intent.setAction(Intent.ACTION_MAIN);
                intent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_QUICK_TILE);
                intent.putExtra(PPApplication.EXTRA_PROFILE_ID, Profile.RESTART_EVENTS_PROFILE_ID);
                startActivityAndCollapse(intent);
            }
        } else {
            //
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
}
