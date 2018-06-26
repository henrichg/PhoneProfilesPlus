package sk.henrichg.phoneprofilesplus;

class WifiSSIDData {

    String ssid;
    final String bssid;
    final boolean custom;
    boolean configured;
    boolean scanned;

    //WifiSSIDData() {
    //}

    WifiSSIDData(String ssid, String bssid, boolean custom, boolean configured, boolean scanned)
    {
        this.ssid = ssid;
        this.bssid = bssid;
        this.custom = custom;
        this.configured = configured;
        this.scanned = scanned;
    }

}
