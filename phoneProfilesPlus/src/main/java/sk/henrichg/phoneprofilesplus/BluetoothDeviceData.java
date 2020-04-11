package sk.henrichg.phoneprofilesplus;

class BluetoothDeviceData {

    String name;
    String address;
    int type;
    boolean custom;
    long timestamp;
    boolean configured;
    boolean scanned;

    //BluetoothDeviceData() {
    //}

    BluetoothDeviceData(String name, String address, int type, boolean custom, long timestamp, boolean configured, boolean scanned)
    {
        this.name = name;
        this.address = address;
        this.type = type;
        this.custom = custom;
        this.timestamp = timestamp;
        this.configured = configured;
        this.scanned = scanned;
    }

    String getName() {
        if (name != null)
            return name;
        else
            return "";
    }

    String getAddress() {
        if (address != null)
            return address;
        else
            return "";
    }

    void setName(String name) {
        this.name = name;
    }

}
