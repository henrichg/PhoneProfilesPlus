package sk.henrichg.phoneprofilesplus;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import androidx.annotation.NonNull;

class LocationScannerListener implements LocationListener {

    public void onLocationChanged(@NonNull Location location) {
//            PPApplicationStatic.logE("[IN_LISTENER] LocationScannerListener.onLocationChanged", "xxx");
        LocationScanner.doLocationChanged(location, false);
    }

    public void onProviderDisabled(@NonNull String provider) {
//            PPApplicationStatic.logE("[IN_LISTENER] LocationScanner.LocationScannerListener.onProviderDisabled", "xxx");
    }

    public void onProviderEnabled(@NonNull String provider) {
//            PPApplicationStatic.logE("[IN_LISTENER] LocationScanner.LocationScannerListener.onProviderEnabled", "xxx");
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
//            PPApplicationStatic.logE("[IN_LISTENER] LocationScanner.LocationScannerListener.onStatusChanged", "xxx");
    }
}
