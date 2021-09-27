package sk.henrichg.phoneprofilesplus;

import android.content.ComponentName;

class LiveWallpapersData {

    String wallpaperName;
    ComponentName componentName;

    // constructor is required for GSon !!!
    @SuppressWarnings("unused")
    LiveWallpapersData() {
    }

    LiveWallpapersData(String wallpaperName, ComponentName componentName)
    {
        this.wallpaperName = wallpaperName;
        this.componentName = componentName;
    }

}
