package sk.henrichg.phoneprofilesplus;

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
                if (!PhoneProfilesService.displayPreferencesErrorNotification(profile, null, getApplicationContext())) {
//                    PPApplication.logE("&&&&&&& PPTileService.onClick", "called is DataWrapper.activateProfileFromMainThread");
                    dataWrapper.activateProfileFromMainThread(profile, false, PPApplication.STARTUP_SOURCE_QUICK_TILE, false, this, false);
                } else
                    dataWrapper.finishActivity(PPApplication.STARTUP_SOURCE_QUICK_TILE, false, this);
            }
            else
                dataWrapper.finishActivity(PPApplication.STARTUP_SOURCE_QUICK_TILE, false, this);
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
