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

class BluetoothConnectedDevices {
    private static BluetoothHeadset mBluetoothHeadset;
    private static BluetoothHealth mBluetoothHealth;
    private static BluetoothA2dp mBluetoothA2dp;

    static List<BluetoothDeviceData> getConnectedDevices(Context context) {
        final List<BluetoothDeviceData> connectedDevices = new ArrayList<>();

        BluetoothAdapter bluetoothAdapter = BluetoothScanJob.getBluetoothAdapter(context);
        if (bluetoothAdapter != null) {
            BluetoothProfile.ServiceListener profileListener = new BluetoothProfile.ServiceListener() {
                public void onServiceConnected(int profile, BluetoothProfile proxy) {
                    if (profile == BluetoothProfile.HEADSET) {
                        PPApplication.logE("------ BluetoothConnectedDevices.getConnectedDevices", "HEADSET service connected");
                        mBluetoothHeadset = (BluetoothHeadset) proxy;
                    }
                    if (profile == BluetoothProfile.HEALTH) {
                        PPApplication.logE("------ BluetoothConnectedDevices.getConnectedDevices", "HEALTH service connected");
                        mBluetoothHealth = (BluetoothHealth) proxy;
                    }
                    if (profile == BluetoothProfile.A2DP) {
                        PPApplication.logE("------ BluetoothConnectedDevices.getConnectedDevices", "A2DP service connected");
                        mBluetoothA2dp = (BluetoothA2dp) proxy;
                    }
                }

                public void onServiceDisconnected(int profile) {
                    if (profile == BluetoothProfile.HEADSET) {
                        PPApplication.logE("------ BluetoothConnectedDevices.getConnectedDevices", "HEADSET service disconnected");
                        mBluetoothHeadset = null;
                    }
                    if (profile == BluetoothProfile.HEALTH) {
                        PPApplication.logE("------ BluetoothConnectedDevices.getConnectedDevices", "HEALTH service disconnected");
                        mBluetoothHealth = null;
                    }
                    if (profile == BluetoothProfile.A2DP) {
                        PPApplication.logE("------ BluetoothConnectedDevices.getConnectedDevices", "A2DP service disconnected");
                        mBluetoothA2dp = null;
                    }
                }
            };

            try {
                List<BluetoothDevice> devices;

                PPApplication.logE("------ BluetoothConnectedDevices.getConnectedDevices", "A2DP start");
                bluetoothAdapter.getProfileProxy(context, profileListener, BluetoothProfile.A2DP);
                int i = 30;
                while ((mBluetoothA2dp == null) && (i > 0)) {
                    PPApplication.sleep(100);
                    --i;
                }
                if (mBluetoothA2dp != null) {
                    devices = mBluetoothA2dp.getConnectedDevices();
                    PPApplication.logE("------ BluetoothConnectedDevices.getConnectedDevices", "A2DP size=" + devices.size());
                    addConnectedDevices(devices, connectedDevices);
                    bluetoothAdapter.closeProfileProxy(BluetoothProfile.A2DP, mBluetoothA2dp);
                    i = 30;
                    while ((mBluetoothA2dp != null) && (i > 0)) {
                        PPApplication.sleep(100);
                        --i;
                    }
                }

                PPApplication.logE("------ BluetoothConnectedDevices.getConnectedDevices", "HEADSET start");
                bluetoothAdapter.getProfileProxy(context, profileListener, BluetoothProfile.HEADSET);
                i = 30;
                while ((mBluetoothHeadset == null) && (i > 0)) {
                    PPApplication.sleep(100);
                    --i;
                }
                if (mBluetoothHeadset != null) {
                    devices = mBluetoothHeadset.getConnectedDevices();
                    PPApplication.logE("------ BluetoothConnectedDevices.getConnectedDevices", "HEADSET size=" + devices.size());
                    addConnectedDevices(devices, connectedDevices);
                    bluetoothAdapter.closeProfileProxy(BluetoothProfile.HEADSET, mBluetoothHeadset);
                    i = 30;
                    while ((mBluetoothHeadset != null) && (i > 0)) {
                        PPApplication.sleep(100);
                        --i;
                    }
                }

                PPApplication.logE("------ BluetoothConnectedDevices.getConnectedDevices", "HEALTH start");
                bluetoothAdapter.getProfileProxy(context, profileListener, BluetoothProfile.HEALTH);
                i = 30;
                while ((mBluetoothHealth == null) && (i > 0)) {
                    PPApplication.sleep(100);
                    --i;
                }
                if (mBluetoothHealth != null) {
                    devices = mBluetoothHealth.getConnectedDevices();
                    PPApplication.logE("------ BluetoothConnectedDevices.getConnectedDevices", "HEALTH size=" + devices.size());
                    addConnectedDevices(devices, connectedDevices);
                    bluetoothAdapter.closeProfileProxy(BluetoothProfile.HEALTH, mBluetoothHealth);
                    i = 30;
                    while ((mBluetoothHealth != null) && (i > 0)) {
                        PPApplication.sleep(100);
                        --i;
                    }
                }

                //if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    final BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
                    if (bluetoothManager != null) {
                        devices = bluetoothManager.getConnectedDevices(BluetoothProfile.GATT);
                        addConnectedDevices(devices, connectedDevices);
                        devices = bluetoothManager.getConnectedDevices(BluetoothProfile.GATT_SERVER);
                        addConnectedDevices(devices, connectedDevices);
                    }
                //}
            } catch (Exception ignored) {}
        }

        return connectedDevices;
    }

    private static void addConnectedDevices(List<BluetoothDevice> detectedDevices, List<BluetoothDeviceData> connectedDevices)
    {
        //synchronized (PPApplication.bluetoothConnectionChangeStateMutex) {
            for (BluetoothDevice device : detectedDevices) {
                PPApplication.logE("------ BluetoothConnectedDevices.addConnectedDevice", "device.name=" + device.getName());
                PPApplication.logE("------ BluetoothConnectedDevices.addConnectedDevice", "device.address=" + device.getAddress());
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
                PPApplication.logE("------ BluetoothConnectedDevices.addConnectedDevice", "found=" + found);
                if (!found) {
                    int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();
                    Calendar now = Calendar.getInstance();
                    long timestamp = now.getTimeInMillis() - gmtOffset;
                    connectedDevices.add(new BluetoothDeviceData(device.getName(), device.getAddress(),
                            BluetoothScanJob.getBluetoothType(device), false, timestamp));
                }
            }
        //}
    }

    /*
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
    */

}
