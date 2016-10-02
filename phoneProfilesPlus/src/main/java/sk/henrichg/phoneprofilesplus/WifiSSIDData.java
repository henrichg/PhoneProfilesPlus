package sk.henrichg.phoneprofilesplus;

public class WifiSSIDData {

    public String ssid;
    public String bssid;
    public boolean custom;

    public WifiSSIDData() {
    }

    public WifiSSIDData(String ssid, String bssid, boolean custom)
    {
        this.ssid = ssid;
        this.bssid = bssid;
        this.custom = custom;
    }

}
