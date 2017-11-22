package sk.henrichg.phoneprofilesplus;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothHealth;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

class ConnectedBluetoothDevices {

    private static BluetoothAdapter mBluetoothAdapter;

    private static BluetoothHeadset mBluetoothHeadset;
    private static BluetoothHealth mBluetoothHealth;
    private static BluetoothA2dp mBluetoothA2dp;

    static List<BluetoothDeviceData> getConnectedDevices(Context context) {
        final List<BluetoothDeviceData> connectedDevices = new ArrayList<>();

        mBluetoothAdapter = BluetoothScanJob.getBluetoothAdapter(context);
        BluetoothProfile.ServiceListener profileListener = new BluetoothProfile.ServiceListener() {
            public void onServiceConnected(int profile, BluetoothProfile proxy) {
                if (profile == BluetoothProfile.HEADSET) {
                    PPApplication.logE("------ ConnectedBluetoothDevices.addConnectedDevice", "HEADSET service connected");
                    mBluetoothHeadset = (BluetoothHeadset) proxy;
                }
                if (profile == BluetoothProfile.HEALTH) {
                    PPApplication.logE("------ ConnectedBluetoothDevices.addConnectedDevice", "HEALTH service connected");
                    mBluetoothHealth = (BluetoothHealth) proxy;
                }
                if (profile == BluetoothProfile.A2DP) {
                    PPApplication.logE("------ ConnectedBluetoothDevices.addConnectedDevice", "A2DP service connected");
                    mBluetoothA2dp = (BluetoothA2dp) proxy;
                }
            }
            public void onServiceDisconnected(int profile) {
                if (profile == BluetoothProfile.HEADSET) {
                    PPApplication.logE("------ ConnectedBluetoothDevices.addConnectedDevice", "HEADSET service disconnected");
                    mBluetoothHeadset = null;
                }
                if (profile == BluetoothProfile.HEALTH) {
                    PPApplication.logE("------ ConnectedBluetoothDevices.addConnectedDevice", "HEALTH service disconnected");
                    mBluetoothHealth = null;
                }
                if (profile == BluetoothProfile.A2DP) {
                    PPApplication.logE("------ ConnectedBluetoothDevices.addConnectedDevice", "A2DP service disconnected");
                    mBluetoothA2dp = null;
                }
            }
        };
        mBluetoothAdapter.getProfileProxy(context, profileListener, BluetoothProfile.HEADSET);
        mBluetoothAdapter.getProfileProxy(context, profileListener, BluetoothProfile.HEALTH);
        mBluetoothAdapter.getProfileProxy(context, profileListener, BluetoothProfile.A2DP);

        PPApplication.sleep(500);

        List<BluetoothDevice> devices = mBluetoothHeadset.getConnectedDevices();
        addConnectedDevices(devices, connectedDevices);
        mBluetoothAdapter.closeProfileProxy(BluetoothProfile.HEADSET, mBluetoothHeadset);

        devices = mBluetoothHealth.getConnectedDevices();
        addConnectedDevices(devices, connectedDevices);
        mBluetoothAdapter.closeProfileProxy(BluetoothProfile.HEALTH, mBluetoothHealth);

        devices = mBluetoothA2dp.getConnectedDevices();
        addConnectedDevices(devices, connectedDevices);
        mBluetoothAdapter.closeProfileProxy(BluetoothProfile.A2DP, mBluetoothA2dp);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            final BluetoothManager bluetoothManager = (BluetoothManager)context.getSystemService(Context.BLUETOOTH_SERVICE);
            if (bluetoothManager != null) {
                devices = bluetoothManager.getConnectedDevices(BluetoothProfile.GATT);
                addConnectedDevices(devices, connectedDevices);
                devices = bluetoothManager.getConnectedDevices(BluetoothProfile.GATT_SERVER);
                addConnectedDevices(devices, connectedDevices);
            }
        }

        return connectedDevices;
    }

    private static void addConnectedDevices(List<BluetoothDevice> detectedDevices, List<BluetoothDeviceData> connectedDevices)
    {
        //synchronized (PPApplication.bluetoothConnectionChangeStateMutex) {
            for (BluetoothDevice device : detectedDevices) {
                PPApplication.logE("------ ConnectedBluetoothDevices.addConnectedDevice", "device.name=" + device.getName());
                PPApplication.logE("------ ConnectedBluetoothDevices.addConnectedDevice", "device.address=" + device.getAddress());
                boolean found = false;
                for (BluetoothDeviceData _device : connectedDevices) {
                    if (_device.address.equals(device.getAddress())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    for (BluetoothDeviceData _device : connectedDevices) {
                        if (_device.getName().equalsIgnoreCase(device.getName())) {
                            found = true;
                            break;
                        }
                    }
                }
                PPApplication.logE("------ ConnectedBluetoothDevices.addConnectedDevice", "found=" + found);
                if (!found) {
                    connectedDevices.add(new BluetoothDeviceData(device.getName(), device.getAddress(),
                            BluetoothScanJob.getBluetoothType(device), false, 0));
                }
            }
        //}
    }

    static boolean isBluetoothConnected(List<BluetoothDeviceData> connectedDevices, BluetoothDeviceData deviceData, String sensorDeviceName)
    {
        //synchronized (PPApplication.bluetoothConnectionChangeStateMutex) {
            if ((deviceData == null) && sensorDeviceName.isEmpty())
                return (connectedDevices != null) && (connectedDevices.size() > 0);
            else {
                if (connectedDevices != null) {
                    if (deviceData != null) {
                        boolean found = false;
                        for (BluetoothDeviceData _device : connectedDevices) {
                            if (_device.address.equals(deviceData.getAddress())) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            for (BluetoothDeviceData _device : connectedDevices) {
                                if (_device.getName().equalsIgnoreCase(deviceData.getName())) {
                                    found = true;
                                    break;
                                }
                            }
                        }
                        return found;
                    }
                    else {
                        for (BluetoothDeviceData _device : connectedDevices) {
                            String device = _device.getName().toUpperCase();
                            String _adapterName = sensorDeviceName.toUpperCase();
                            if (Wildcard.match(device, _adapterName, '_', '%', true)) {
                                return true;
                            }
                        }
                    }
                }
                return false;
            }
        //}
    }

}
