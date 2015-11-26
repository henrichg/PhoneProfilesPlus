package sk.henrichg.phoneprofilesplus;

public class BluetoothDeviceData {

    public String name;
    public String address;
    public boolean le;

    public BluetoothDeviceData() {
    }

    public BluetoothDeviceData(String name, String address, boolean le)
    {
        this.name = name;
        this.address = address;
        this.le = le;
    }

    public String getName() {
        if (name != null)
            return name;
        else
            return "";
    }

    public void setName(String name) {
        this.name = name;
    }

}
