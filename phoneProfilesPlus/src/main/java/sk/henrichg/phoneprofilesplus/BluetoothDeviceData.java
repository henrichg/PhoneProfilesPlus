package sk.henrichg.phoneprofilesplus;

import java.util.Objects;

class BluetoothDeviceData {

    private String name;
    private final String address;
    final int type;
    final boolean custom;
    final long timestamp;
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
        return Objects.requireNonNullElse(name, "");
    }

    String getAddress() {
        return Objects.requireNonNullElse(address, "");
    }

    void setName(String name) {
        this.name = name;
    }

}
