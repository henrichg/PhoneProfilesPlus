package sk.henrichg.phoneprofilesplus;

public class BluetoothDeviceData {
	
	private String name;
	public String address;
	
    public BluetoothDeviceData() {
    }

    public BluetoothDeviceData(String name, String address) 
    {
    	this.name = name;
    	this.address = address;
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
