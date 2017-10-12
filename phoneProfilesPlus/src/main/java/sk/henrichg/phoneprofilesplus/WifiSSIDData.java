package sk.henrichg.phoneprofilesplus;

class WifiSSIDData {

    String ssid;
    final String bssid;
    final boolean custom;

    //WifiSSIDData() {
    //}

    WifiSSIDData(String ssid, String bssid, boolean custom)
    {
        this.ssid = ssid;
        this.bssid = bssid;
        this.custom = custom;
    }

}
