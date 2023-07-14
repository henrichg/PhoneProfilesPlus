package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothHealth;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Build;

import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

class BluetoothConnectedDevices {

    private static volatile BluetoothHeadset bluetoothHeadset = null;
    private static volatile BluetoothHealth bluetoothHealth = null;
    private static volatile BluetoothA2dp bluetoothA2dp = null;

    private static volatile BluetoothProfile.ServiceListener profileListener = null;

    @SuppressLint("MissingPermission")
    static void getConnectedDevices(final Context context, final boolean _callEventHandler) {
        final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); //BluetoothScanWorker.getBluetoothAdapter(context);
        if (bluetoothAdapter != null) {
            if (!bluetoothAdapter.isEnabled()) {

                final Context appContext = context.getApplicationContext();

                BluetoothConnectionBroadcastReceiver.clearConnectedDevices(appContext, false);
                BluetoothConnectionBroadcastReceiver.saveConnectedDevices(appContext);
                if (_callEventHandler)
                    callEventHandler(appContext);

//                Log.e("BluetoothConnectedDevices.getConnectedDevices", "(xxx) END of getConnectedDevices");

                return;
            }

// HandlerThread is not needed, this method is already called from it in PhoneProfilesService.doFirstStart()

            if (profileListener == null) {
                profileListener = new BluetoothProfile.ServiceListener() {
                    public void onServiceConnected(int profile, BluetoothProfile proxy) {

//                        Log.e("BluetoothConnectedDevices.getConnectedDevices", "[1] start of onServiceConnected");

                        if (profile == BluetoothProfile.HEADSET) {
//                            PPApplicationStatic.logE("[IN_LISTENER] BluetoothConnectedDevices.onServiceConnected", "BluetoothProfile.HEADSET");
                            bluetoothHeadset = (BluetoothHeadset) proxy;

                            final Context appContext = context.getApplicationContext();

                            if (bluetoothHeadset != null) {
                                try {
                                    List<BluetoothDevice> devices = bluetoothHeadset.getConnectedDevices();
                                    final List<BluetoothDeviceData> connectedDevices = new ArrayList<>();
                                    addConnectedDevices(devices, connectedDevices);
                                    BluetoothConnectionBroadcastReceiver.addConnectedDeviceData(connectedDevices);
                                    BluetoothConnectionBroadcastReceiver.saveConnectedDevices(appContext);
                                    if (_callEventHandler)
                                        callEventHandler(appContext);
                                } catch (Exception e) {
                                    // not log this, profile may not exists
                                    //Log.e("BluetoothConnectedDevices.getConnectedDevices", Log.getStackTraceString(e));
                                    //PPApplicationStatic.recordException(e);
                                }
                                bluetoothAdapter.closeProfileProxy(BluetoothProfile.HEADSET, bluetoothHeadset);
                            }
                        }
                        if (profile == BluetoothProfile.HEALTH) {
//                            PPApplicationStatic.logE("[IN_LISTENER] BluetoothConnectedDevices.onServiceConnected", "BluetoothProfile.HEALTH");
                            bluetoothHealth = (BluetoothHealth) proxy;

                            final Context appContext = context.getApplicationContext();

                            if (bluetoothHealth != null) {
                                try {
                                    @SuppressWarnings("deprecation")
                                    List<BluetoothDevice> devices = bluetoothHealth.getConnectedDevices();
                                    final List<BluetoothDeviceData> connectedDevices = new ArrayList<>();
                                    addConnectedDevices(devices, connectedDevices);
                                    BluetoothConnectionBroadcastReceiver.addConnectedDeviceData(connectedDevices);
                                    BluetoothConnectionBroadcastReceiver.saveConnectedDevices(appContext);
                                    if (_callEventHandler)
                                        callEventHandler(appContext);
                                } catch (Exception e) {
                                    // not log this, profile may not exists
                                    //Log.e("BluetoothConnectedDevices.getConnectedDevices", Log.getStackTraceString(e));
                                    //PPApplicationStatic.recordException(e);
                                }
                                bluetoothAdapter.closeProfileProxy(BluetoothProfile.HEALTH, bluetoothHealth);
                            }
                        }
                        if (profile == BluetoothProfile.A2DP) {
//                            PPApplicationStatic.logE("[IN_LISTENER] BluetoothConnectedDevices.onServiceConnected", "BluetoothProfile.A2DP");
                            bluetoothA2dp = (BluetoothA2dp) proxy;

                            final Context appContext = context.getApplicationContext();

                            if (bluetoothA2dp != null) {
                                try {
                                    List<BluetoothDevice> devices = bluetoothA2dp.getConnectedDevices();
                                    final List<BluetoothDeviceData> connectedDevices = new ArrayList<>();
                                    addConnectedDevices(devices, connectedDevices);
                                    BluetoothConnectionBroadcastReceiver.addConnectedDeviceData(connectedDevices);
                                    BluetoothConnectionBroadcastReceiver.saveConnectedDevices(appContext);
                                    if (_callEventHandler)
                                        callEventHandler(appContext);
                                } catch (Exception e) {
                                    // not log this, profile may not exists
                                    //Log.e("BluetoothConnectedDevices.getConnectedDevices", Log.getStackTraceString(e));
                                    //PPApplicationStatic.recordException(e);
                                }
                                bluetoothAdapter.closeProfileProxy(BluetoothProfile.A2DP, bluetoothA2dp);
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
                    }
                };
            }

            try {
                bluetoothHeadset = null;
                bluetoothHealth = null;
                bluetoothA2dp = null;

                BluetoothConnectionBroadcastReceiver.clearConnectedDevices(context, false);
                BluetoothConnectionBroadcastReceiver.saveConnectedDevices(context);

                bluetoothAdapter.getProfileProxy(context, profileListener, BluetoothProfile.A2DP);

                bluetoothAdapter.getProfileProxy(context, profileListener, BluetoothProfile.HEADSET);

                if (Build.VERSION.SDK_INT < 29) {
                    bluetoothAdapter.getProfileProxy(context, profileListener, BluetoothProfile.HEALTH);
                }

                final Context appContext = context.getApplicationContext();
                final BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
                if (bluetoothManager != null) {
                    final List<BluetoothDeviceData> connectedDevices = new ArrayList<>();
                    //final Context appContext = context.getApplicationContext();
                    List<BluetoothDevice> devices;

                    devices = bluetoothManager.getConnectedDevices(BluetoothProfile.GATT);
                    addConnectedDevices(devices, connectedDevices);

                    devices = bluetoothManager.getConnectedDevices(BluetoothProfile.GATT_SERVER);
                    addConnectedDevices(devices, connectedDevices);

//                    devices = bluetoothManager.getConnectedDevices(BluetoothProfile.SAP);
//                    addConnectedDevices(devices, connectedDevices);

                    BluetoothConnectionBroadcastReceiver.addConnectedDeviceData(connectedDevices);
                    BluetoothConnectionBroadcastReceiver.saveConnectedDevices(appContext);
                    if (_callEventHandler)
                        callEventHandler(appContext);
                }

//                Log.e("BluetoothConnectedDevices.getConnectedDevices", "[1] END of getConnectedDevices");

            } catch (Exception e) {
                //Log.e("BluetoothConnectedDevices.getConnectedDevices", Log.getStackTraceString(e));
                PPApplicationStatic.recordException(e);
            }
        }
    }

    @SuppressLint("MissingPermission")
    private static void addConnectedDevices(List<BluetoothDevice> detectedDevices, List<BluetoothDeviceData> connectedDevices)
    {
        //synchronized (PPApplication.bluetoothConnectionChangeStateMutex) {
            for (BluetoothDevice device : detectedDevices) {
//                PPApplicationStatic.logE("[IN_LISTENER] BluetoothConnectedDevices.addConnectedDevices", "device.name="+device.getName());
//                PPApplicationStatic.logE("[IN_LISTENER] BluetoothConnectedDevices.addConnectedDevices", "device.address="+device.getAddress());
//                Log.e("BluetoothConnectedDevices.addConnectedDevices", "[1] device.name="+device.getName());
//                Log.e("BluetoothConnectedDevices.addConnectedDevices", "[1] device.address="+device.getAddress());

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

    private static void callEventHandler(Context appContext) {
//        PPApplicationStatic.logE("[IN_LISTENER] BluetoothConnectedDevices.callEventHandler", "xxxxxxxxxxxxxxxxxxxx");

        if (ApplicationPreferences.prefEventBluetoothScanRequest ||
                ApplicationPreferences.prefEventBluetoothLEScanRequest ||
                ApplicationPreferences.prefEventBluetoothWaitForResult ||
                ApplicationPreferences.prefEventBluetoothLEWaitForResult ||
                ApplicationPreferences.prefEventBluetoothEnabledForScan)
            PhoneProfilesServiceStatic.cancelBluetoothWorker(appContext, true, false);

//        Log.e("BluetoothConnectedDevices.callEventHandler", "[1] enqueue MainWorker");

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
                //                            PPApplicationStatic.logE("[WORKER_CALL] PhoneProfilesService.doCommand", "xxx");
                //workManager.enqueue(worker);
                workManager.enqueueUniqueWork(MainWorker.HANDLE_EVENTS_BLUETOOTH_CONNECTION_WORK_TAG, ExistingWorkPolicy.REPLACE, worker);
//                }
            }
        } catch (Exception e) {
            PPApplicationStatic.recordException(e);
        }
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
