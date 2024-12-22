package sk.henrichg.phoneprofilesplus;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattServer;
// is from reginer android.jar
import android.bluetooth.BluetoothHapClient;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothHealth;
import android.bluetooth.BluetoothHearingAid;
import android.bluetooth.BluetoothHidDevice;
import android.bluetooth.BluetoothLeAudio;
import android.bluetooth.BluetoothProfile;
// is from reginer android.jar
import android.bluetooth.BluetoothSap;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

class BluetoothConnectedDevicesDetector {

    private static volatile BluetoothHeadset bluetoothHeadset = null;
    private static volatile BluetoothHealth bluetoothHealth = null;
    private static volatile BluetoothA2dp bluetoothA2dp = null;
    private static volatile BluetoothHearingAid bluetoothHearingAid = null;
    private static volatile BluetoothGatt bluetoothGatt = null;
    private static volatile BluetoothGattServer bluetoothGattServer = null;
    private static volatile BluetoothHapClient bluetoothHapClient = null;
    private static volatile BluetoothHidDevice bluetoothHidDevice = null;
    private static volatile BluetoothLeAudio bluetoothLeAudio = null;
    private static volatile BluetoothSap bluetoothSap = null;


    private static volatile BluetoothProfile.ServiceListener profileListener = null;

    private static Context appContext = null;

    @SuppressLint("MissingPermission")
    static void getConnectedDevices(final Context context, final boolean _callEventHandler) {
        final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); //BluetoothScanWorker.getBluetoothAdapter(context);
        if (bluetoothAdapter != null) {
            appContext = context.getApplicationContext();

            if (!bluetoothAdapter.isEnabled()) {
                List<BluetoothDeviceData> connectedDevices = BluetoothConnectionBroadcastReceiver.getConnectedDevices(appContext);
                BluetoothConnectionBroadcastReceiver.clearConnectedDevices(connectedDevices/*appContext, false*/);
                // this also clears shared preferences
                BluetoothConnectionBroadcastReceiver.saveConnectedDevices(connectedDevices, appContext);
                if (_callEventHandler)
                    callEventHandler();

//                PPApplicationStatic.logE("[IN_LISTENER] BluetoothConnectedDevicesDetector.getConnectedDevices", "(BT not enabled) END of getConnectedDevices");

                return;
            }

// HandlerThread is not needed, this method is already called from it in PhoneProfilesService.doFirstStart()

            if (profileListener == null) {
                profileListener = new BluetoothProfile.ServiceListener() {
                    public void onServiceConnected(int profile, BluetoothProfile proxy) {

//                        PPApplicationStatic.logE("[IN_LISTENER] BluetoothConnectedDevicesDetector.onServiceConnected", "[1] start of onServiceConnected");

                        final List<BluetoothDeviceData> connectedDevices = BluetoothConnectionBroadcastReceiver.getConnectedDevices(appContext);
                        //BluetoothConnectionBroadcastReceiver.saveConnectedDevices(connectedDevices, appContext);

                        if (profile == BluetoothProfile.HEADSET) {
//                            PPApplicationStatic.logE("[IN_LISTENER] BluetoothConnectedDevicesDetector.onServiceConnected", "BluetoothProfile.HEADSET");
                            bluetoothHeadset = (BluetoothHeadset) proxy;
//                            PPApplicationStatic.logE("[IN_LISTENER] BluetoothConnectedDevicesDetector.onServiceConnected", "bluetoothHeadset="+bluetoothHeadset);

                            //final Context appContext = context.getApplicationContext();

                            if (bluetoothHeadset != null) {
                                try {
                                    List<BluetoothDevice> detectedDevices = bluetoothHeadset.getConnectedDevices();
                                    final List<BluetoothDeviceData> connectedDevicesToAdd = new ArrayList<>();
//                                    PPApplicationStatic.logE("[IN_LISTENER] BluetoothConnectedDevicesDetector.onServiceConnected", "detectedDevices="+detectedDevices);
                                    addConnectedDevices(detectedDevices, connectedDevicesToAdd);
                                    BluetoothConnectionBroadcastReceiver.addConnectedDeviceData(connectedDevices, connectedDevicesToAdd);
                                    BluetoothConnectionBroadcastReceiver.saveConnectedDevices(connectedDevices, appContext);
                                    if (_callEventHandler && (!detectedDevices.isEmpty()))
                                        callEventHandler();
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

                            //final Context appContext = context.getApplicationContext();

                            if (bluetoothHealth != null) {
                                try {
                                    @SuppressWarnings("deprecation")
                                    List<BluetoothDevice> detectedDevices = bluetoothHealth.getConnectedDevices();
                                    final List<BluetoothDeviceData> connectedDevicesToAdd = new ArrayList<>();
//                                    PPApplicationStatic.logE("[IN_LISTENER] BluetoothConnectedDevicesDetector.onServiceConnected", "detectedDevices="+detectedDevices);
                                    addConnectedDevices(detectedDevices, connectedDevicesToAdd);
                                    BluetoothConnectionBroadcastReceiver.addConnectedDeviceData(connectedDevices, connectedDevicesToAdd);
                                    BluetoothConnectionBroadcastReceiver.saveConnectedDevices(connectedDevices, appContext);
                                    if (_callEventHandler && (!detectedDevices.isEmpty()))
                                        callEventHandler();
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

                            //final Context appContext = context.getApplicationContext();

                            if (bluetoothA2dp != null) {
                                try {
                                    List<BluetoothDevice> detectedDevices = bluetoothA2dp.getConnectedDevices();
                                    final List<BluetoothDeviceData> connectedDevicesToAdd = new ArrayList<>();
//                                    PPApplicationStatic.logE("[IN_LISTENER] BluetoothConnectedDevicesDetector.onServiceConnected", "detectedDevices="+detectedDevices);
                                    addConnectedDevices(detectedDevices, connectedDevicesToAdd);
                                    BluetoothConnectionBroadcastReceiver.addConnectedDeviceData(connectedDevices, connectedDevicesToAdd);
                                    BluetoothConnectionBroadcastReceiver.saveConnectedDevices(connectedDevices, appContext);
                                    if (_callEventHandler && (!detectedDevices.isEmpty()))
                                        callEventHandler();
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

                                //final Context appContext = context.getApplicationContext();

                                if (bluetoothHearingAid != null) {
                                    try {
                                        List<BluetoothDevice> detectedDevices = bluetoothHearingAid.getConnectedDevices();
                                        final List<BluetoothDeviceData> connectedDevicesToAdd = new ArrayList<>();
//                                        PPApplicationStatic.logE("[IN_LISTENER] BluetoothConnectedDevicesDetector.onServiceConnected", "detectedDevices="+detectedDevices);
                                        addConnectedDevices(detectedDevices, connectedDevicesToAdd);
                                        BluetoothConnectionBroadcastReceiver.addConnectedDeviceData(connectedDevices, connectedDevicesToAdd);
                                        BluetoothConnectionBroadcastReceiver.saveConnectedDevices(connectedDevices, appContext);
                                        if (_callEventHandler && (!detectedDevices.isEmpty()))
                                            callEventHandler();
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

                            //final Context appContext = context.getApplicationContext();

                            if (bluetoothGatt != null) {
                                try {
                                    //noinspection deprecation
                                    List<BluetoothDevice> detectedDevices = bluetoothGatt.getConnectedDevices();
                                    final List<BluetoothDeviceData> connectedDevicesToAdd = new ArrayList<>();
//                                    PPApplicationStatic.logE("[IN_LISTENER] BluetoothConnectedDevicesDetector.onServiceConnected", "detectedDevices="+detectedDevices);
                                    addConnectedDevices(detectedDevices, connectedDevicesToAdd);
                                    BluetoothConnectionBroadcastReceiver.addConnectedDeviceData(connectedDevices, connectedDevicesToAdd);
                                    BluetoothConnectionBroadcastReceiver.saveConnectedDevices(connectedDevices, appContext);
                                    if (_callEventHandler && (!detectedDevices.isEmpty()))
                                        callEventHandler();
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

                            //final Context appContext = context.getApplicationContext();

                            if (bluetoothGattServer != null) {
                                try {
                                    List<BluetoothDevice> detectedDevices = bluetoothGattServer.getConnectedDevices();
                                    final List<BluetoothDeviceData> connectedDevicesToAdd = new ArrayList<>();
//                                    PPApplicationStatic.logE("[IN_LISTENER] BluetoothConnectedDevicesDetector.onServiceConnected", "detectedDevices="+detectedDevices);
                                    addConnectedDevices(detectedDevices, connectedDevicesToAdd);
                                    BluetoothConnectionBroadcastReceiver.addConnectedDeviceData(connectedDevices, connectedDevicesToAdd);
                                    BluetoothConnectionBroadcastReceiver.saveConnectedDevices(connectedDevices, appContext);
                                    if (_callEventHandler && (!detectedDevices.isEmpty()))
                                        callEventHandler();
                                } catch (Exception e) {
                                    // not log this, profile may not exists
                                    //Log.e("BluetoothConnectedDevicesDetector.getConnectedDevices", Log.getStackTraceString(e));
                                    //PPApplicationStatic.recordException(e);
                                }
                                bluetoothAdapter.closeProfileProxy(BluetoothProfile.GATT_SERVER, bluetoothGattServer);
                            }
                        }
                        if (Build.VERSION.SDK_INT >= 33) {
                            if (profile == BluetoothProfile.HAP_CLIENT) {
//                                PPApplicationStatic.logE("[IN_LISTENER] BluetoothConnectedDevicesDetector.onServiceConnected", "BluetoothProfile.HAP_CLIENT");
                                bluetoothHapClient = (BluetoothHapClient) proxy;
//                                PPApplicationStatic.logE("[IN_LISTENER] BluetoothConnectedDevicesDetector.onServiceConnected", "bluetoothHapClient=" + bluetoothHapClient);

                                //final Context appContext = context.getApplicationContext();

                                if (bluetoothHapClient != null) {
                                    try {
                                        List<BluetoothDevice> detectedDevices = bluetoothHapClient.getConnectedDevices();
                                        final List<BluetoothDeviceData> connectedDevicesToAdd = new ArrayList<>();
//                                        PPApplicationStatic.logE("[IN_LISTENER] BluetoothConnectedDevicesDetector.onServiceConnected", "detectedDevices="+detectedDevices);
                                        addConnectedDevices(detectedDevices, connectedDevicesToAdd);
                                        BluetoothConnectionBroadcastReceiver.addConnectedDeviceData(connectedDevices, connectedDevicesToAdd);
                                        BluetoothConnectionBroadcastReceiver.saveConnectedDevices(connectedDevices, appContext);
                                        if (_callEventHandler && (!detectedDevices.isEmpty()))
                                            callEventHandler();
                                    } catch (Exception e) {
                                        // not log this, profile may not exists
                                        //Log.e("BluetoothConnectedDevicesDetector.getConnectedDevices", Log.getStackTraceString(e));
                                        //PPApplicationStatic.recordException(e);
                                    }
                                    bluetoothAdapter.closeProfileProxy(BluetoothProfile.HAP_CLIENT, bluetoothHapClient);
                                }
                            }
                        }
                        if (Build.VERSION.SDK_INT >= 28) {
                            if (profile == BluetoothProfile.HID_DEVICE) {
//                                PPApplicationStatic.logE("[IN_LISTENER] BluetoothConnectedDevicesDetector.onServiceConnected", "BluetoothProfile.HID_DEVICE");
                                bluetoothHidDevice = (BluetoothHidDevice) proxy;
//                                PPApplicationStatic.logE("[IN_LISTENER] BluetoothConnectedDevicesDetector.onServiceConnected", "bluetoothHidDevice=" + bluetoothHidDevice);

                                //final Context appContext = context.getApplicationContext();

                                if (bluetoothHidDevice != null) {
                                    try {
                                        List<BluetoothDevice> detectedDevices = bluetoothHidDevice.getConnectedDevices();
                                        final List<BluetoothDeviceData> connectedDevicesToAdd = new ArrayList<>();
//                                        PPApplicationStatic.logE("[IN_LISTENER] BluetoothConnectedDevicesDetector.onServiceConnected", "detectedDevices="+detectedDevices);
                                        addConnectedDevices(detectedDevices, connectedDevicesToAdd);
                                        BluetoothConnectionBroadcastReceiver.addConnectedDeviceData(connectedDevices, connectedDevicesToAdd);
                                        BluetoothConnectionBroadcastReceiver.saveConnectedDevices(connectedDevices, appContext);
                                        if (_callEventHandler && (!detectedDevices.isEmpty()))
                                            callEventHandler();
                                    } catch (Exception e) {
                                        // not log this, profile may not exists
                                        //Log.e("BluetoothConnectedDevicesDetector.getConnectedDevices", Log.getStackTraceString(e));
                                        //PPApplicationStatic.recordException(e);
                                    }
                                    bluetoothAdapter.closeProfileProxy(BluetoothProfile.HID_DEVICE, bluetoothHidDevice);
                                }
                            }
                        }
                        if (Build.VERSION.SDK_INT >= 33) {
                            if (profile == BluetoothProfile.LE_AUDIO) {
//                                PPApplicationStatic.logE("[IN_LISTENER] BluetoothConnectedDevicesDetector.onServiceConnected", "BluetoothProfile.LE_AUDIO");
                                bluetoothLeAudio = (BluetoothLeAudio) proxy;
//                                PPApplicationStatic.logE("[IN_LISTENER] BluetoothConnectedDevicesDetector.onServiceConnected", "bluetoothLeAudio=" + bluetoothLeAudio);

                                //final Context appContext = context.getApplicationContext();

                                if (bluetoothLeAudio != null) {
                                    try {
                                        List<BluetoothDevice> detectedDevices = bluetoothLeAudio.getConnectedDevices();
                                        final List<BluetoothDeviceData> connectedDevicesToAdd = new ArrayList<>();
//                                        PPApplicationStatic.logE("[IN_LISTENER] BluetoothConnectedDevicesDetector.onServiceConnected", "detectedDevices="+detectedDevices);
                                        addConnectedDevices(detectedDevices, connectedDevicesToAdd);
                                        BluetoothConnectionBroadcastReceiver.addConnectedDeviceData(connectedDevices, connectedDevicesToAdd);
                                        BluetoothConnectionBroadcastReceiver.saveConnectedDevices(connectedDevices, appContext);
                                        if (_callEventHandler && (!detectedDevices.isEmpty()))
                                            callEventHandler();
                                    } catch (Exception e) {
                                        // not log this, profile may not exists
                                        //Log.e("BluetoothConnectedDevicesDetector.getConnectedDevices", Log.getStackTraceString(e));
                                        //PPApplicationStatic.recordException(e);
                                    }
                                    bluetoothAdapter.closeProfileProxy(BluetoothProfile.LE_AUDIO, bluetoothLeAudio);
                                }
                            }
                        }
                        if (profile == BluetoothProfile.SAP) {
//                            PPApplicationStatic.logE("[IN_LISTENER] BluetoothConnectedDevicesDetector.onServiceConnected", "BluetoothProfile.SAP");
                            bluetoothSap = (BluetoothSap) proxy;
//                            PPApplicationStatic.logE("[IN_LISTENER] BluetoothConnectedDevicesDetector.onServiceConnected", "bluetoothSap=" + bluetoothSap);

                            //final Context appContext = context.getApplicationContext();

                            if (bluetoothSap != null) {
                                try {
                                    List<BluetoothDevice> detectedDevices = bluetoothSap.getConnectedDevices();
                                    final List<BluetoothDeviceData> connectedDevicesToAdd = new ArrayList<>();
//                                    PPApplicationStatic.logE("[IN_LISTENER] BluetoothConnectedDevicesDetector.onServiceConnected", "detectedDevices="+detectedDevices);
                                    addConnectedDevices(detectedDevices, connectedDevicesToAdd);
                                    BluetoothConnectionBroadcastReceiver.addConnectedDeviceData(connectedDevices, connectedDevicesToAdd);
                                    BluetoothConnectionBroadcastReceiver.saveConnectedDevices(connectedDevices, appContext);
                                    if (_callEventHandler && (!detectedDevices.isEmpty()))
                                        callEventHandler();
                                } catch (Exception e) {
                                    // not log this, profile may not exists
                                    //Log.e("BluetoothConnectedDevicesDetector.getConnectedDevices", Log.getStackTraceString(e));
                                    //PPApplicationStatic.recordException(e);
                                }
                                bluetoothAdapter.closeProfileProxy(BluetoothProfile.SAP, bluetoothSap);
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
                        if (profile == BluetoothProfile.HAP_CLIENT) {
                            bluetoothHapClient = null;
                        }
                        if (profile == BluetoothProfile.HID_DEVICE) {
                            bluetoothHidDevice = null;
                        }
                        if (profile == BluetoothProfile.LE_AUDIO) {
                            bluetoothLeAudio = null;
                        }
                        if (profile == BluetoothProfile.SAP) {
                            bluetoothSap = null;
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
                bluetoothHapClient = null;
                bluetoothHidDevice = null;
                bluetoothLeAudio = null;
                bluetoothSap = null;

                //final Context appContext = context.getApplicationContext();

                List<BluetoothDeviceData> _connectedDevices = new ArrayList<>();
                BluetoothConnectionBroadcastReceiver.clearConnectedDevices(_connectedDevices/*appContext, false*/);
                // this also clears shared preferences
                BluetoothConnectionBroadcastReceiver.saveConnectedDevices(_connectedDevices, appContext);
                //if (_callEventHandler)
                //    callEventHandler(appContext);

//                PPApplicationStatic.logE("[IN_LISTENER] BluetoothConnectedDevicesDetector.getConnectedDevices", "*** start of profilelisener ***");

                if (Permissions.hasPermission(context, Manifest.permission.BLUETOOTH)) {
                    if (bluetoothAdapter.getState() == BluetoothAdapter.STATE_ON) {

                        // workaround for check connection status of bounded devices
                        // working also for BLE devices
                        @SuppressLint("MissingPermission")
                        Set<BluetoothDevice> boundedDevices = bluetoothAdapter.getBondedDevices();
                        if (boundedDevices != null) {
                            boolean deviceDetected = false;
                            for (BluetoothDevice boundedDevice : boundedDevices) {
                                try {
                                    Method m = BluetoothDevice.class.getMethod("isConnected");
                                    Boolean o = (Boolean) m.invoke(boundedDevice);
                                    if ((o != null) && o) {
                                        deviceDetected = true;
//                                        PPApplicationStatic.logE("[IN_LISTENER] BluetoothConnectedDevicesDetector.getConnectedDevices", "(****) device connected=" + boundedDevice.getName());
//                                        PPApplicationStatic.logE("[IN_LISTENER] BluetoothConnectedDevicesDetector.getConnectedDevices", "(****) address=" + boundedDevice.getAddress());
                                        final List<BluetoothDeviceData> connectedDevicesToAdd = new ArrayList<>();
                                        List<BluetoothDevice> detectedDevices = new ArrayList<>();
                                        detectedDevices.add(boundedDevice);
                                        addConnectedDevices(detectedDevices, connectedDevicesToAdd);
                                        final List<BluetoothDeviceData> connectedDevices = BluetoothConnectionBroadcastReceiver.getConnectedDevices(appContext);
                                        BluetoothConnectionBroadcastReceiver.addConnectedDeviceData(connectedDevices, connectedDevicesToAdd);
                                        BluetoothConnectionBroadcastReceiver.saveConnectedDevices(connectedDevices, appContext);
                                    }
                                } catch (Exception e) {
                                    Log.e("BluetoothConnectedDevicesDetector.getConnectedDevices", Log.getStackTraceString(e));
                                }
                            }
                            if (_callEventHandler && deviceDetected)
                                callEventHandler();
                        }

                        bluetoothAdapter.getProfileProxy(context, profileListener, BluetoothProfile.A2DP);
                        bluetoothAdapter.getProfileProxy(context, profileListener, BluetoothProfile.HEADSET);
                        //if (Build.VERSION.SDK_INT < 29)
                        bluetoothAdapter.getProfileProxy(context, profileListener, BluetoothProfile.HEALTH);
                        if (Build.VERSION.SDK_INT >= 29)
                            bluetoothAdapter.getProfileProxy(context, profileListener, BluetoothProfile.HEARING_AID);
                        bluetoothAdapter.getProfileProxy(context, profileListener, BluetoothProfile.GATT);
                        bluetoothAdapter.getProfileProxy(context, profileListener, BluetoothProfile.GATT_SERVER);
                        if (Build.VERSION.SDK_INT >= 33)
                            bluetoothAdapter.getProfileProxy(context, profileListener, BluetoothProfile.HAP_CLIENT);
                        if (Build.VERSION.SDK_INT >= 28)
                            bluetoothAdapter.getProfileProxy(context, profileListener, BluetoothProfile.HID_DEVICE);
                        if (Build.VERSION.SDK_INT >= 33)
                            bluetoothAdapter.getProfileProxy(context, profileListener, BluetoothProfile.LE_AUDIO);
                        bluetoothAdapter.getProfileProxy(context, profileListener, BluetoothProfile.SAP);

                    }
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
            for (BluetoothDevice detectedDevice : detectedDevices) {
                boolean found = false;

                String detectedName = detectedDevice.getName();
                String detectedAddress = detectedDevice.getAddress();
                if (!detectedAddress.isEmpty()) {
                    // do not add device without name

                    //String detectedAddress = detecedDevice.getAddress();
                    for (BluetoothDeviceData connectedDevice : connectedDevices) {
                        if ((!connectedDevice.getAddress().isEmpty()) &&
                                connectedDevice.getAddress().equals(detectedAddress)) {
                            found = true;
                            break;
                        }
                    }
                    if ((!found) && (detectedName != null) && (!detectedName.isEmpty())){
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
                        connectedDevices.add(new BluetoothDeviceData(detectedName, detectedAddress,
                                BluetoothScanWorker.getBluetoothType(detectedDevice), false, timestamp, false, false));
                    }
                }
            }
        //}
    }

    private static void callEventHandler() {
//        PPApplicationStatic.logE("[IN_LISTENER] BluetoothConnectedDevicesDetector.callEventHandler", "xxxxxxxxxxxxxxxxxxxx");
//        PPApplicationStatic.logE("[MAIN_WORKER_CALL] BluetoothConnectedDevicesDetector.callEventHandler", "xxxxxxxxxxxxxxxxxxxx");

        if (ApplicationPreferences.prefEventBluetoothScanRequest ||
                ApplicationPreferences.prefEventBluetoothLEScanRequest ||
                ApplicationPreferences.prefEventBluetoothWaitForResult ||
                ApplicationPreferences.prefEventBluetoothLEWaitForResult ||
                ApplicationPreferences.prefEventBluetoothEnabledForScan)
            PhoneProfilesServiceStatic.cancelBluetoothWorker(appContext, true, false);

//        Log.e("BluetoothConnectedDevicesDetector.callEventHandler", "[1] enqueue MainWorker");

        OneTimeWorkRequest worker =
                new OneTimeWorkRequest.Builder(MainWorker.class)
                        .addTag(MainWorker.HANDLE_EVENTS_BLUETOOTH_CONNECTION_WORK_TAG)
                        //.setInputData(workData)
                        .setInitialDelay(10, TimeUnit.SECONDS)
                        //.keepResultsForAtLeast(PPApplication.WORK_PRUNE_DELAY_MINUTES, TimeUnit.MINUTES)
                        .build();
        try {
//            if (PPApplicationStatic.getApplicationStarted(true, true)) {
            WorkManager workManager = PPApplication.getWorkManagerInstance();
            if (workManager != null) {

                //                            //if (PPApplicationStatic.logEnabled()) {
                //                            ListenableFuture<List<WorkInfo>> statuses;
                //                            statuses = workManager.getWorkInfosForUniqueWork(MainWorker.HANDLE_EVENTS_VOLUMES_WORK_TAG);
                //                            try {
                //                                List<WorkInfo> workInfoList = statuses.get();
                //                            } catch (Exception ignored) {
                //                            }
                //                            //}
                //
//                PPApplicationStatic.logE("[WORKER_CALL] BluetoothConnectionBroadcastReceiver.callEventHandler", "xxx");
                //workManager.enqueue(worker);
                workManager.enqueueUniqueWork(MainWorker.HANDLE_EVENTS_BLUETOOTH_CONNECTION_WORK_TAG, ExistingWorkPolicy.REPLACE, worker);
//                }
            }
        } catch (Exception e) {
            PPApplicationStatic.recordException(e);
        }
    }

}
