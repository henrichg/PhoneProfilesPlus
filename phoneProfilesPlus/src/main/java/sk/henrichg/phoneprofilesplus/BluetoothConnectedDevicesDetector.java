package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothHealth;
import android.bluetooth.BluetoothHearingAid;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Build;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

class BluetoothConnectedDevicesDetector {

    private static volatile BluetoothHeadset bluetoothHeadset = null;
    private static volatile BluetoothHealth bluetoothHealth = null;
    private static volatile BluetoothA2dp bluetoothA2dp = null;
    private static volatile BluetoothHearingAid bluetoothHearingAid = null;
    private static volatile BluetoothGatt bluetoothGatt = null;
    private static volatile BluetoothGattServer bluetoothGattServer = null;


    private static volatile BluetoothProfile.ServiceListener profileListener = null;

    @SuppressLint("MissingPermission")
    static void getConnectedDevices(final Context context/*, final boolean _callEventHandler*/) {
        final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); //BluetoothScanWorker.getBluetoothAdapter(context);
        if (bluetoothAdapter != null) {
            if (!bluetoothAdapter.isEnabled()) {

                final Context appContext = context.getApplicationContext();

                List<BluetoothDeviceData> connectedDevices = BluetoothConnectionBroadcastReceiver.getConnectedDevices(appContext);
                BluetoothConnectionBroadcastReceiver.clearConnectedDevices(connectedDevices/*appContext, false*/);
                // this also clears shared preferences
                BluetoothConnectionBroadcastReceiver.saveConnectedDevices(connectedDevices, appContext);
                //if (_callEventHandler)
                //    callEventHandler(appContext);

//                PPApplicationStatic.logE("[IN_LISTENER] BluetoothConnectedDevicesDetector.getConnectedDevices", "(xxx) END of getConnectedDevices");

                return;
            }

// HandlerThread is not needed, this method is already called from it in PhoneProfilesService.doFirstStart()

            if (profileListener == null) {
                profileListener = new BluetoothProfile.ServiceListener() {
                    public void onServiceConnected(int profile, BluetoothProfile proxy) {

//                        PPApplicationStatic.logE("[IN_LISTENER] BluetoothConnectedDevicesDetector.getConnectedDevices", "[1] start of onServiceConnected");

                        final List<BluetoothDeviceData> connectedDevices = new ArrayList<>();

                        if (profile == BluetoothProfile.HEADSET) {
//                            PPApplicationStatic.logE("[IN_LISTENER] BluetoothConnectedDevicesDetector.onServiceConnected", "BluetoothProfile.HEADSET");
                            bluetoothHeadset = (BluetoothHeadset) proxy;
//                            PPApplicationStatic.logE("[IN_LISTENER] BluetoothConnectedDevicesDetector.onServiceConnected", "bluetoothHeadset="+bluetoothHeadset);

                            final Context appContext = context.getApplicationContext();

                            if (bluetoothHeadset != null) {
                                try {
                                    List<BluetoothDevice> devices = bluetoothHeadset.getConnectedDevices();
                                    final List<BluetoothDeviceData> connectedDevicesToAdd = new ArrayList<>();
//                                    PPApplicationStatic.logE("[IN_LISTENER] BluetoothConnectedDevicesDetector.onServiceConnected", "devices="+devices);
                                    addConnectedDevices(devices, connectedDevicesToAdd);
                                    BluetoothConnectionBroadcastReceiver.addConnectedDeviceData(connectedDevices, connectedDevicesToAdd);
                                    BluetoothConnectionBroadcastReceiver.saveConnectedDevices(connectedDevices, appContext);
                                    //if (_callEventHandler)
                                    //    callEventHandler(appContext);
                                } catch (Exception e) {
                                    // not log this, profile may not exists
                                    //Log.e("BluetoothConnectedDevicesDetector.getConnectedDevices", Log.getStackTraceString(e));
                                    //PPApplicationStatic.recordException(e);
                                }
                                bluetoothAdapter.closeProfileProxy(BluetoothProfile.HEADSET, bluetoothHeadset);
                            }
                        }
                        if (profile == BluetoothProfile.HEALTH) {
//                            PPApplicationStatic.logE("[IN_LISTENER] BluetoothConnectedDevicesDetector.onServiceConnected", "BluetoothProfile.HEALTH");
                            bluetoothHealth = (BluetoothHealth) proxy;
//                            PPApplicationStatic.logE("[IN_LISTENER] BluetoothConnectedDevicesDetector.onServiceConnected", "bluetoothHealth="+bluetoothHealth);

                            final Context appContext = context.getApplicationContext();

                            if (bluetoothHealth != null) {
                                try {
                                    @SuppressWarnings("deprecation")
                                    List<BluetoothDevice> devices = bluetoothHealth.getConnectedDevices();
                                    final List<BluetoothDeviceData> connectedDevicesToAdd = new ArrayList<>();
//                                    PPApplicationStatic.logE("[IN_LISTENER] BluetoothConnectedDevicesDetector.onServiceConnected", "devices="+devices);
                                    addConnectedDevices(devices, connectedDevicesToAdd);
                                    BluetoothConnectionBroadcastReceiver.addConnectedDeviceData(connectedDevices, connectedDevicesToAdd);
                                    BluetoothConnectionBroadcastReceiver.saveConnectedDevices(connectedDevices, appContext);
                                    //if (_callEventHandler)
                                    //    callEventHandler(appContext);
                                } catch (Exception e) {
                                    // not log this, profile may not exists
                                    //Log.e("BluetoothConnectedDevicesDetector.getConnectedDevices", Log.getStackTraceString(e));
                                    //PPApplicationStatic.recordException(e);
                                }
                                bluetoothAdapter.closeProfileProxy(BluetoothProfile.HEALTH, bluetoothHealth);
                            }
                        }
                        if (profile == BluetoothProfile.A2DP) {
//                            PPApplicationStatic.logE("[IN_LISTENER] BluetoothConnectedDevicesDetector.onServiceConnected", "BluetoothProfile.A2DP");
                            bluetoothA2dp = (BluetoothA2dp) proxy;
//                            PPApplicationStatic.logE("[IN_LISTENER] BluetoothConnectedDevicesDetector.onServiceConnected", "bluetoothA2dp="+bluetoothA2dp);

                            final Context appContext = context.getApplicationContext();

                            if (bluetoothA2dp != null) {
                                try {
                                    List<BluetoothDevice> devices = bluetoothA2dp.getConnectedDevices();
                                    final List<BluetoothDeviceData> connectedDevicesToAdd = new ArrayList<>();
//                                    PPApplicationStatic.logE("[IN_LISTENER] BluetoothConnectedDevicesDetector.onServiceConnected", "devices="+devices);
                                    addConnectedDevices(devices, connectedDevicesToAdd);
                                    BluetoothConnectionBroadcastReceiver.addConnectedDeviceData(connectedDevices, connectedDevicesToAdd);
                                    BluetoothConnectionBroadcastReceiver.saveConnectedDevices(connectedDevices, appContext);
                                    //if (_callEventHandler)
                                    //    callEventHandler(appContext);
                                } catch (Exception e) {
                                    // not log this, profile may not exists
                                    //Log.e("BluetoothConnectedDevicesDetector.getConnectedDevices", Log.getStackTraceString(e));
                                    //PPApplicationStatic.recordException(e);
                                }
                                bluetoothAdapter.closeProfileProxy(BluetoothProfile.A2DP, bluetoothA2dp);
                            }
                        }
                        if (Build.VERSION.SDK_INT >= 29) {
                            if (profile == BluetoothProfile.HEARING_AID) {
//                                PPApplicationStatic.logE("[IN_LISTENER] BluetoothConnectedDevicesDetector.onServiceConnected", "BluetoothProfile.HEARING_AID");
                                bluetoothHearingAid = (BluetoothHearingAid) proxy;
//                                PPApplicationStatic.logE("[IN_LISTENER] BluetoothConnectedDevicesDetector.onServiceConnected", "bluetoothHearingAid=" + bluetoothHearingAid);

                                final Context appContext = context.getApplicationContext();

                                if (bluetoothHearingAid != null) {
                                    try {
                                        List<BluetoothDevice> devices = bluetoothHearingAid.getConnectedDevices();
                                        final List<BluetoothDeviceData> connectedDevicesToAdd = new ArrayList<>();
//                                    PPApplicationStatic.logE("[IN_LISTENER] BluetoothConnectedDevicesDetector.onServiceConnected", "devices="+devices);
                                        addConnectedDevices(devices, connectedDevicesToAdd);
                                        BluetoothConnectionBroadcastReceiver.addConnectedDeviceData(connectedDevices, connectedDevicesToAdd);
                                        BluetoothConnectionBroadcastReceiver.saveConnectedDevices(connectedDevices, appContext);
                                        //if (_callEventHandler)
                                        //    callEventHandler(appContext);
                                    } catch (Exception e) {
                                        // not log this, profile may not exists
                                        //Log.e("BluetoothConnectedDevicesDetector.getConnectedDevices", Log.getStackTraceString(e));
                                        //PPApplicationStatic.recordException(e);
                                    }
                                    bluetoothAdapter.closeProfileProxy(BluetoothProfile.HEARING_AID, bluetoothHearingAid);
                                }
                            }
                        }
                        if (profile == BluetoothProfile.GATT) {
//                            PPApplicationStatic.logE("[IN_LISTENER] BluetoothConnectedDevicesDetector.onServiceConnected", "BluetoothProfile.GATT");
                            bluetoothGatt = (BluetoothGatt) proxy;
//                            PPApplicationStatic.logE("[IN_LISTENER] BluetoothConnectedDevicesDetector.onServiceConnected", "bluetoothGatt=" + bluetoothGatt);

                            final Context appContext = context.getApplicationContext();

                            if (bluetoothGatt != null) {
                                try {
                                    //noinspection deprecation
                                    List<BluetoothDevice> devices = bluetoothGatt.getConnectedDevices();
                                    final List<BluetoothDeviceData> connectedDevicesToAdd = new ArrayList<>();
//                                    PPApplicationStatic.logE("[IN_LISTENER] BluetoothConnectedDevicesDetector.onServiceConnected", "devices="+devices);
                                    addConnectedDevices(devices, connectedDevicesToAdd);
                                    BluetoothConnectionBroadcastReceiver.addConnectedDeviceData(connectedDevices, connectedDevicesToAdd);
                                    BluetoothConnectionBroadcastReceiver.saveConnectedDevices(connectedDevices, appContext);
                                    //if (_callEventHandler)
                                    //    callEventHandler(appContext);
                                } catch (Exception e) {
                                    // not log this, profile may not exists
                                    //Log.e("BluetoothConnectedDevicesDetector.getConnectedDevices", Log.getStackTraceString(e));
                                    //PPApplicationStatic.recordException(e);
                                }
                                bluetoothAdapter.closeProfileProxy(BluetoothProfile.GATT, bluetoothGatt);
                            }
                        }
                        if (profile == BluetoothProfile.GATT_SERVER) {
//                            PPApplicationStatic.logE("[IN_LISTENER] BluetoothConnectedDevicesDetector.onServiceConnected", "BluetoothProfile.GATT_SERVER");
                            bluetoothGattServer = (BluetoothGattServer) proxy;
//                            PPApplicationStatic.logE("[IN_LISTENER] BluetoothConnectedDevicesDetector.onServiceConnected", "bluetoothGattServer=" + bluetoothGattServer);

                            final Context appContext = context.getApplicationContext();

                            if (bluetoothGattServer != null) {
                                try {
                                    List<BluetoothDevice> devices = bluetoothGattServer.getConnectedDevices();
                                    final List<BluetoothDeviceData> connectedDevicesToAdd = new ArrayList<>();
//                                    PPApplicationStatic.logE("[IN_LISTENER] BluetoothConnectedDevicesDetector.onServiceConnected", "devices="+devices);
                                    addConnectedDevices(devices, connectedDevicesToAdd);
                                    BluetoothConnectionBroadcastReceiver.addConnectedDeviceData(connectedDevices, connectedDevicesToAdd);
                                    BluetoothConnectionBroadcastReceiver.saveConnectedDevices(connectedDevices, appContext);
                                    //if (_callEventHandler)
                                    //    callEventHandler(appContext);
                                } catch (Exception e) {
                                    // not log this, profile may not exists
                                    //Log.e("BluetoothConnectedDevicesDetector.getConnectedDevices", Log.getStackTraceString(e));
                                    //PPApplicationStatic.recordException(e);
                                }
                                bluetoothAdapter.closeProfileProxy(BluetoothProfile.GATT_SERVER, bluetoothGattServer);
                            }
                        }
                    }

                    public void onServiceDisconnected(int profile) {
                        if (profile == BluetoothProfile.HEADSET) {
                            bluetoothHeadset = null;
                        }
                        if (profile == BluetoothProfile.HEALTH) {
                            bluetoothHealth = null;
                        }
                        if (profile == BluetoothProfile.A2DP) {
                            bluetoothA2dp = null;
                        }
                        if (profile == BluetoothProfile.HEARING_AID) {
                            bluetoothHearingAid = null;
                        }
                        if (profile == BluetoothProfile.GATT) {
                            bluetoothGatt = null;
                        }
                        if (profile == BluetoothProfile.GATT_SERVER) {
                            bluetoothGattServer = null;
                        }
                    }
                };
            }

            try {
                bluetoothHeadset = null;
                bluetoothHealth = null;
                bluetoothA2dp = null;
                bluetoothHearingAid = null;
                bluetoothGatt = null;
                bluetoothGattServer = null;

                final Context appContext = context.getApplicationContext();

                List<BluetoothDeviceData> connectedDevices = new ArrayList<>();
                BluetoothConnectionBroadcastReceiver.clearConnectedDevices(connectedDevices/*appContext, false*/);
                // this also clears shared preferences
                BluetoothConnectionBroadcastReceiver.saveConnectedDevices(connectedDevices, appContext);
                //if (_callEventHandler)
                //    callEventHandler(appContext);

                bluetoothAdapter.getProfileProxy(context, profileListener, BluetoothProfile.A2DP);
                bluetoothAdapter.getProfileProxy(context, profileListener, BluetoothProfile.HEADSET);
                if (Build.VERSION.SDK_INT < 29) {
                    bluetoothAdapter.getProfileProxy(context, profileListener, BluetoothProfile.HEALTH);
                }
                if (Build.VERSION.SDK_INT >= 29) {
                    bluetoothAdapter.getProfileProxy(context, profileListener, BluetoothProfile.HEARING_AID);
                }
                bluetoothAdapter.getProfileProxy(context, profileListener, BluetoothProfile.GATT);
                bluetoothAdapter.getProfileProxy(context, profileListener, BluetoothProfile.GATT_SERVER);

                final BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
                if (bluetoothManager != null) {
                    final List<BluetoothDeviceData> connectedDevicesToAdd = new ArrayList<>();
                    //final Context appContext = context.getApplicationContext();
                    List<BluetoothDevice> devices;

                    devices = bluetoothManager.getConnectedDevices(BluetoothProfile.GATT);
                    addConnectedDevices(devices, connectedDevicesToAdd);

                    devices = bluetoothManager.getConnectedDevices(BluetoothProfile.GATT_SERVER);
                    addConnectedDevices(devices, connectedDevicesToAdd);

//                    devices = bluetoothManager.getConnectedDevices(BluetoothProfile.SAP);
//                    addConnectedDevices(devices, connectedDevicesToAdd);

                    BluetoothConnectionBroadcastReceiver.addConnectedDeviceData(connectedDevices, connectedDevicesToAdd);
                    BluetoothConnectionBroadcastReceiver.saveConnectedDevices(connectedDevices, appContext);
                    //if (_callEventHandler)
                    //    callEventHandler(appContext);
                }

//                PPApplicationStatic.logE("[IN_LISTENER] BluetoothConnectedDevicesDetector.getConnectedDevices", "[1] END of getConnectedDevices");

            } catch (Exception e) {
                //Log.e("BluetoothConnectedDevicesDetector.getConnectedDevices", Log.getStackTraceString(e));
                PPApplicationStatic.recordException(e);
            }
        }
    }

    @SuppressLint("MissingPermission")
    private static void addConnectedDevices(List<BluetoothDevice> detectedDevices, List<BluetoothDeviceData> connectedDevices)
    {
        //synchronized (PPApplication.bluetoothConnectionChangeStateMutex) {
            for (BluetoothDevice detecedDevice : detectedDevices) {
//                PPApplicationStatic.logE("[IN_LISTENER] BluetoothConnectedDevicesDetector.addConnectedDevices", "detecedDevice.name="+detecedDevice.getName());
//                PPApplicationStatic.logE("[IN_LISTENER] BluetoothConnectedDevicesDetector.addConnectedDevices", "detecedDevice.address="+detecedDevice.getAddress());
//                Log.e("BluetoothConnectedDevicesDetector.addConnectedDevices", "[1] detecedDevice.name="+detecedDevice.getName());
//                Log.e("BluetoothConnectedDevicesDetector.addConnectedDevices", "[1] detecedDevice.address="+detecedDevice.getAddress());

                boolean found = false;

                String detectedName = detecedDevice.getAddress();
                if (!detectedName.isEmpty()) {
                    // do not add device without name

                    String detectedAddress = detecedDevice.getAddress();
                    if (!detectedAddress.isEmpty()) {
                        for (BluetoothDeviceData connectedDevice : connectedDevices) {
                            if ((!connectedDevice.getAddress().isEmpty()) &&
                                    connectedDevice.getAddress().equals(detectedAddress)) {
                                found = true;
                                break;
                            }
                        }
                    }
                    if (!found) {
                        for (BluetoothDeviceData connectedDevice : connectedDevices) {
                            String connectedName = connectedDevice.getName();
                            if ((!connectedName.isEmpty()) &&
                                    connectedName.equalsIgnoreCase(detectedName)) {
                                found = true;
                                break;
                            }
                        }
                    }
                    if (!found) {
                        int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();
                        Calendar now = Calendar.getInstance();
                        long timestamp = now.getTimeInMillis() - gmtOffset;
                        connectedDevices.add(new BluetoothDeviceData(detecedDevice.getName(), detecedDevice.getAddress(),
                                BluetoothScanWorker.getBluetoothType(detecedDevice), false, timestamp, false, false));
                    }
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
