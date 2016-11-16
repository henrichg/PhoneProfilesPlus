package sk.henrichg.phoneprofilesplus;

class BluetoothDeviceData {

    String name;
    String address;
    int type;
    boolean custom;

    //BluetoothDeviceData() {
    //}

    BluetoothDeviceData(String name, String address, int type, boolean custom)
    {
        this.name = name;
        this.address = address;
        this.type = type;
        this.custom = custom;
    }

    String getName() {
        if (name != null)
            return name;
        else
            return "";
    }

    void setName(String name) {
        this.name = name;
    }

}
