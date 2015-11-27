package sk.henrichg.phoneprofilesplus;

public class BluetoothDeviceData {

    public String name;
    public String address;
    public int type;

    public BluetoothDeviceData() {
    }

    public BluetoothDeviceData(String name, String address, int type)
    {
        this.name = name;
        this.address = address;
        this.type = type;
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
