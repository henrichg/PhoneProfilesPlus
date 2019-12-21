package sk.henrichg.phoneprofilesplus;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothHealth;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemPropertiesProto;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

class BluetoothConnectedDevices {

    private static boolean bluetoothHeadsetStarted = false;
    private static boolean bluetoothHealthStarted = false;
    private static boolean bluetoothA2dpStarted = false;

    private static boolean bluetoothHeadsetEnd = false;
    private static boolean bluetoothHealthEnd = false;
    private static boolean bluetoothA2dpEnd = false;

    private static BluetoothHeadset bluetoothHeadset = null;
    private static BluetoothHealth bluetoothHealth = null;
    private static BluetoothA2dp bluetoothA2dp = null;

    @SuppressWarnings("FieldCanBeLocal")
    private static boolean okA2DP = false;
    @SuppressWarnings("FieldCanBeLocal")
    private static boolean okHEADSET = false;
    @SuppressWarnings("FieldCanBeLocal")
    private static boolean okHEALTH = false;

    static void getConnectedDevices(final Context context) {
        final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); //BluetoothScanWorker.getBluetoothAdapter(context);
        if (bluetoothAdapter != null) {
            if (!bluetoothAdapter.isEnabled())
                return;

            final BluetoothProfile.ServiceListener profileListener = new BluetoothProfile.ServiceListener() {
                public void onServiceConnected(int profile, BluetoothProfile proxy) {
                    if (profile == BluetoothProfile.HEADSET) {
                        PPApplication.logE("------ BluetoothConnectedDevices.getConnectedDevices.onServiceConnected", "HEADSET service connected");

                        bluetoothHeadsetStarted = true;
                        bluetoothHeadset = (BluetoothHeadset) proxy;

                        final Context appContext = context.getApplicationContext();
                        PPApplication.startHandlerThreadBluetoothConnectedDevices();
                        final Handler handler = new Handler(PPApplication.handlerThreadBluetoothConnectedDevices.getLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
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
                                        PPApplication.logE("------ BluetoothConnectedDevices.getConnectedDevices", "HEADSET end");
                                        bluetoothHeadsetEnd = true;
                                        //bluetoothAdapter.closeProfileProxy(BluetoothProfile.HEADSET, bluetoothHeadset);
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

                        bluetoothHealthStarted = true;
                        bluetoothHealth = (BluetoothHealth) proxy;

                        final Context appContext = context.getApplicationContext();
                        PPApplication.startHandlerThreadBluetoothConnectedDevices();
                        final Handler handler = new Handler(PPApplication.handlerThreadBluetoothConnectedDevices.getLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
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
                                        PPApplication.logE("------ BluetoothConnectedDevices.getConnectedDevices", "HEALTH end");
                                        bluetoothHealthEnd = true;
                                        //bluetoothAdapter.closeProfileProxy(BluetoothProfile.HEALTH, bluetoothHealth);
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

                        bluetoothA2dpStarted = true;
                        bluetoothA2dp = (BluetoothA2dp) proxy;

                        final Context appContext = context.getApplicationContext();
                        PPApplication.startHandlerThreadBluetoothConnectedDevices();
                        final Handler handler = new Handler(PPApplication.handlerThreadBluetoothConnectedDevices.getLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
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
                                        PPApplication.logE("------ BluetoothConnectedDevices.getConnectedDevices", "A2DP end");
                                        bluetoothA2dpEnd = true;
                                        //bluetoothAdapter.closeProfileProxy(BluetoothProfile.A2DP, bluetoothA2dp);
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
                        bluetoothHeadset = null;
                    }
                    if (profile == BluetoothProfile.HEALTH) {
                        PPApplication.logE("------ BluetoothConnectedDevices.getConnectedDevices.onServiceDisconnected", "HEALTH service disconnected");
                        bluetoothHealth = null;
                    }
                    if (profile == BluetoothProfile.A2DP) {
                        PPApplication.logE("------ BluetoothConnectedDevices.getConnectedDevices.onServiceDisconnected", "A2DP service disconnected");
                        bluetoothA2dp = null;
                    }
                }
            };

            try {

                okA2DP = bluetoothAdapter.getProfileProxy(context, profileListener, BluetoothProfile.A2DP);
                PPApplication.logE("------ BluetoothConnectedDevices.getConnectedDevices", "A2DP start="+okA2DP);

                okHEADSET = bluetoothAdapter.getProfileProxy(context, profileListener, BluetoothProfile.HEADSET);
                PPApplication.logE("------ BluetoothConnectedDevices.getConnectedDevices", "HEADSET start="+okHEADSET);

                if (Build.VERSION.SDK_INT < 29) {
                    okHEALTH = bluetoothAdapter.getProfileProxy(context, profileListener, BluetoothProfile.HEALTH);
                    PPApplication.logE("------ BluetoothConnectedDevices.getConnectedDevices", "HEALTH start=" + okHEALTH);
                }

                // wait for start
                if (Build.VERSION.SDK_INT < 29) {
                    for (int i = 0; i < 100; i++) {
                        if ((!bluetoothHeadsetStarted) || (!bluetoothHealthStarted) || (!bluetoothA2dpStarted))
                            PPApplication.sleep(100);
                        else
                            break;
                    }
                }
                else {
                    for (int i = 0; i < 100; i++) {
                        if ((!bluetoothHeadsetStarted) || (!bluetoothA2dpStarted))
                            PPApplication.sleep(100);
                        else
                            break;
                    }
                }

                // wait for end
                if (Build.VERSION.SDK_INT < 29) {
                    for (int i = 0; i < 100; i++) {
                        if ((!bluetoothHeadsetEnd) || (!bluetoothHealthEnd) || (!bluetoothA2dpEnd))
                            PPApplication.sleep(100);
                        else
                            break;
                    }
                }
                else {
                    for (int i = 0; i < 100; i++) {
                        if ((!bluetoothHeadsetEnd) || (!bluetoothA2dpEnd))
                            PPApplication.sleep(100);
                        else
                            break;
                    }
                }

                if (okA2DP && (bluetoothA2dp != null)) {
                    PPApplication.logE("------ BluetoothConnectedDevices.getConnectedDevices", "A2DP close");
                    bluetoothAdapter.closeProfileProxy(BluetoothProfile.A2DP, bluetoothA2dp);
                }

                if (okHEADSET && (bluetoothHeadset != null)) {
                    PPApplication.logE("------ BluetoothConnectedDevices.getConnectedDevices", "HEADSET close");
                    bluetoothAdapter.closeProfileProxy(BluetoothProfile.HEADSET, bluetoothHeadset);
                }

                if (Build.VERSION.SDK_INT < 29) {
                    if (okHEALTH && (bluetoothHealth != null)) {
                        PPApplication.logE("------ BluetoothConnectedDevices.getConnectedDevices", "HEALTH close");
                        bluetoothAdapter.closeProfileProxy(BluetoothProfile.HEALTH, bluetoothHealth);
                    }
                }

                // wait for disconnect
                if (Build.VERSION.SDK_INT < 29) {
                    for (int i = 0; i < 100; i++) {
                        if ((bluetoothHeadset != null) || (bluetoothHealth != null) || (bluetoothA2dp != null))
                            PPApplication.sleep(100);
                        else
                            break;
                    }
                }
                else {
                    for (int i = 0; i < 100; i++) {
                        if ((bluetoothHeadset != null) || (bluetoothA2dp != null))
                            PPApplication.sleep(100);
                        else
                            break;
                    }
                }

                PPApplication.logE("------ BluetoothConnectedDevices.getConnectedDevices", "all disconnected?");

                final Context appContext = context.getApplicationContext();
                PPApplication.startHandlerThreadBluetoothConnectedDevices();
                final Handler handler = new Handler(PPApplication.handlerThreadBluetoothConnectedDevices.getLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
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
                                PPApplication.logE("------ BluetoothConnectedDevices.getConnectedDevices", "GATT size=" + devices.size());
                                addConnectedDevices(devices, connectedDevices);

                                devices = bluetoothManager.getConnectedDevices(BluetoothProfile.GATT_SERVER);
                                PPApplication.logE("------ BluetoothConnectedDevices.getConnectedDevices", "GATT_SERVER size=" + devices.size());
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
                            BluetoothScanWorker.getBluetoothType(device), false, timestamp, false, false));
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
