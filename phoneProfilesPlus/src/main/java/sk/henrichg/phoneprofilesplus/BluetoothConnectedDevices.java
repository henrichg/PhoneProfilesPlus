package sk.henrichg.phoneprofilesplus;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothHealth;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static android.content.Context.POWER_SERVICE;

class BluetoothConnectedDevices {

    static void getConnectedDevices(final Context context) {
        final BluetoothAdapter bluetoothAdapter = BluetoothScanJob.getBluetoothAdapter(context);
        if (bluetoothAdapter != null) {
            if (!bluetoothAdapter.isEnabled())
                return;

            final BluetoothProfile.ServiceListener profileListener = new BluetoothProfile.ServiceListener() {
                public void onServiceConnected(int profile, BluetoothProfile proxy) {
                    if (profile == BluetoothProfile.HEADSET) {
                        PPApplication.logE("------ BluetoothConnectedDevices.getConnectedDevices.onServiceConnected", "HEADSET service connected");

                        final BluetoothHeadset bluetoothHeadset = (BluetoothHeadset) proxy;

                        final Context appContext = context.getApplicationContext();
                        PPApplication.startHandlerThreadBluetoothConnectedDevices();
                        final Handler handler = new Handler(PPApplication.handlerThreadBluetoothConnectedDevices.getLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                                PowerManager.WakeLock wakeLock = null;
                                try {
                                    if (powerManager != null) {
                                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":BluetoothConnectedDevices_getConnectedDevices_HEADSET");
                                        wakeLock.acquire(10 * 60 * 1000);
                                    }

                                    if (bluetoothHeadset != null) {
                                        try {
                                            List<BluetoothDevice> devices = bluetoothHeadset.getConnectedDevices();
                                            PPApplication.logE("------ BluetoothConnectedDevices.getConnectedDevices", "HEADSET size=" + devices.size());
                                            final List<BluetoothDeviceData> connectedDevices = new ArrayList<>();
                                            addConnectedDevices(devices, connectedDevices);
                                            BluetoothConnectionBroadcastReceiver.addConnectedDeviceData(connectedDevices);
                                            BluetoothConnectionBroadcastReceiver.saveConnectedDevices(appContext);
                                        } catch (Exception e) {
                                            Log.e("BluetoothConnectedDevices.getConnectedDevices", Log.getStackTraceString(e));
                                        }
                                        PPApplication.logE("------ BluetoothConnectedDevices.getConnectedDevices", "HEADSET started, close it");
                                        bluetoothAdapter.closeProfileProxy(BluetoothProfile.HEADSET, bluetoothHeadset);
                                    }
                                } finally {
                                    if ((wakeLock != null) && wakeLock.isHeld()) {
                                        try {
                                            wakeLock.release();
                                        } catch (Exception ignored) {}
                                    }
                                }
                            }
                        });
                    }
                    if (profile == BluetoothProfile.HEALTH) {
                        PPApplication.logE("------ BluetoothConnectedDevices.getConnectedDevices.onServiceConnected", "HEALTH service connected");

                        final BluetoothHealth bluetoothHealth = (BluetoothHealth) proxy;

                        final Context appContext = context.getApplicationContext();
                        PPApplication.startHandlerThreadBluetoothConnectedDevices();
                        final Handler handler = new Handler(PPApplication.handlerThreadBluetoothConnectedDevices.getLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                                PowerManager.WakeLock wakeLock = null;
                                try {
                                    if (powerManager != null) {
                                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":BluetoothConnectedDevices_getConnectedDevices_HEALTH");
                                        wakeLock.acquire(10 * 60 * 1000);
                                    }

                                    if (bluetoothHealth != null) {
                                        try {
                                            List<BluetoothDevice> devices = bluetoothHealth.getConnectedDevices();
                                            PPApplication.logE("------ BluetoothConnectedDevices.getConnectedDevices", "HEALTH size=" + devices.size());
                                            final List<BluetoothDeviceData> connectedDevices = new ArrayList<>();
                                            addConnectedDevices(devices, connectedDevices);
                                            BluetoothConnectionBroadcastReceiver.addConnectedDeviceData(connectedDevices);
                                            BluetoothConnectionBroadcastReceiver.saveConnectedDevices(appContext);
                                        } catch (Exception e) {
                                            Log.e("BluetoothConnectedDevices.getConnectedDevices", Log.getStackTraceString(e));
                                        }
                                        PPApplication.logE("------ BluetoothConnectedDevices.getConnectedDevices", "HEALTH started, close it");
                                        bluetoothAdapter.closeProfileProxy(BluetoothProfile.HEALTH, bluetoothHealth);
                                    }
                                } finally {
                                    if ((wakeLock != null) && wakeLock.isHeld()) {
                                        try {
                                            wakeLock.release();
                                        } catch (Exception ignored) {}
                                    }
                                }
                            }
                        });
                    }
                    if (profile == BluetoothProfile.A2DP) {
                        PPApplication.logE("------ BluetoothConnectedDevices.getConnectedDevices.onServiceConnected", "A2DP service connected");

                        final BluetoothA2dp bluetoothA2dp = (BluetoothA2dp) proxy;

                        final Context appContext = context.getApplicationContext();
                        PPApplication.startHandlerThreadBluetoothConnectedDevices();
                        final Handler handler = new Handler(PPApplication.handlerThreadBluetoothConnectedDevices.getLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                                PowerManager.WakeLock wakeLock = null;
                                try {
                                    if (powerManager != null) {
                                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":BluetoothConnectedDevices_getConnectedDevices_A2DP");
                                        wakeLock.acquire(10 * 60 * 1000);
                                    }

                                    if (bluetoothA2dp != null) {
                                        try {
                                            List<BluetoothDevice> devices = bluetoothA2dp.getConnectedDevices();
                                            PPApplication.logE("------ BluetoothConnectedDevices.getConnectedDevices", "A2DP size=" + devices.size());
                                            final List<BluetoothDeviceData> connectedDevices = new ArrayList<>();
                                            addConnectedDevices(devices, connectedDevices);
                                            BluetoothConnectionBroadcastReceiver.addConnectedDeviceData(connectedDevices);
                                            BluetoothConnectionBroadcastReceiver.saveConnectedDevices(appContext);
                                        } catch (Exception e) {
                                            Log.e("BluetoothConnectedDevices.getConnectedDevices", Log.getStackTraceString(e));
                                        }
                                        PPApplication.logE("------ BluetoothConnectedDevices.getConnectedDevices", "A2DP started, close it");
                                        bluetoothAdapter.closeProfileProxy(BluetoothProfile.A2DP, bluetoothA2dp);
                                    }
                                } finally {
                                    if ((wakeLock != null) && wakeLock.isHeld()) {
                                        try {
                                            wakeLock.release();
                                        } catch (Exception ignored) {}
                                    }
                                }
                            }
                        });
                    }
                }

                public void onServiceDisconnected(int profile) {
                    if (profile == BluetoothProfile.HEADSET) {
                        PPApplication.logE("------ BluetoothConnectedDevices.getConnectedDevices.onServiceDisconnected", "HEADSET service disconnected");
                    }
                    if (profile == BluetoothProfile.HEALTH) {
                        PPApplication.logE("------ BluetoothConnectedDevices.getConnectedDevices.onServiceDisconnected", "HEALTH service disconnected");
                    }
                    if (profile == BluetoothProfile.A2DP) {
                        PPApplication.logE("------ BluetoothConnectedDevices.getConnectedDevices.onServiceDisconnected", "A2DP service disconnected");
                    }
                }
            };

            try {

                PPApplication.logE("------ BluetoothConnectedDevices.getConnectedDevices", "A2DP start");
                bluetoothAdapter.getProfileProxy(context, profileListener, BluetoothProfile.A2DP);

                PPApplication.logE("------ BluetoothConnectedDevices.getConnectedDevices", "HEADSET start");
                bluetoothAdapter.getProfileProxy(context, profileListener, BluetoothProfile.HEADSET);

                PPApplication.logE("------ BluetoothConnectedDevices.getConnectedDevices", "HEALTH start");
                bluetoothAdapter.getProfileProxy(context, profileListener, BluetoothProfile.HEALTH);

                final Context appContext = context.getApplicationContext();
                PPApplication.startHandlerThreadBluetoothConnectedDevices();
                final Handler handler = new Handler(PPApplication.handlerThreadBluetoothConnectedDevices.getLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                        PowerManager.WakeLock wakeLock = null;
                        try {
                            if (powerManager != null) {
                                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":BluetoothConnectedDevices_getConnectedDevices_GATT");
                                wakeLock.acquire(10 * 60 * 1000);
                            }

                            final BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
                            if (bluetoothManager != null) {
                                final List<BluetoothDeviceData> connectedDevices = new ArrayList<>();
                                final Context appContext = context.getApplicationContext();
                                List<BluetoothDevice> devices;

                                devices = bluetoothManager.getConnectedDevices(BluetoothProfile.GATT);
                                PPApplication.logE("------ BluetoothConnectedDevices.getConnectedDevices.onServiceConnected", "GATT size=" + devices.size());
                                addConnectedDevices(devices, connectedDevices);

                                devices = bluetoothManager.getConnectedDevices(BluetoothProfile.GATT_SERVER);
                                PPApplication.logE("------ BluetoothConnectedDevices.getConnectedDevices.onServiceConnected", "GATT_SERVER size=" + devices.size());
                                addConnectedDevices(devices, connectedDevices);

                                BluetoothConnectionBroadcastReceiver.addConnectedDeviceData(connectedDevices);
                                BluetoothConnectionBroadcastReceiver.saveConnectedDevices(appContext);
                            }
                        } finally {
                            if ((wakeLock != null) && wakeLock.isHeld()) {
                                try {
                                    wakeLock.release();
                                } catch (Exception ignored) {}
                            }
                        }
                    }
                });
            } catch (Exception e) {
                Log.e("BluetoothConnectedDevices.getConnectedDevices", Log.getStackTraceString(e));
            }
        }
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
                            BluetoothScanJob.getBluetoothType(device), false, timestamp, false, false));
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
