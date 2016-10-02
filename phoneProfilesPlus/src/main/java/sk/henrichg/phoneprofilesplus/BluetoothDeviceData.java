package sk.henrichg.phoneprofilesplus;

public class BluetoothDeviceData {

    public String name;
    public String address;
    public int type;
    public boolean custom;

    public BluetoothDeviceData() {
    }

    public BluetoothDeviceData(String name, String address, int type, boolean custom)
    {
        this.name = name;
        this.address = address;
        this.type = type;
        this.custom = custom;
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
