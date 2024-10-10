package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.PowerManager;
import android.os.SystemClock;

import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.gson.Gson;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BluetoothConnectionBroadcastReceiver extends BroadcastReceiver {

    //private static volatile List<BluetoothDeviceData> connectedDevices = null;

    @SuppressLint("MissingPermission")
    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplicationStatic.logE("[IN_BROADCAST] BluetoothConnectionBroadcastReceiver.onReceive", "xxx");

        if (!PPApplicationStatic.getApplicationStarted(true, true))
            // application is not started
            return;

        if (intent == null)
            return;

        final String action = intent.getAction();
        if (action == null)
            return;

        if (action.equals(BluetoothDevice.ACTION_ACL_CONNECTED) ||
                action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED) ||
                action.equals(BluetoothDevice.ACTION_NAME_CHANGED) ||
                action.equals(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED)) {
            // BluetoothConnectionBroadcastReceiver

            PPApplicationStatic.logE("[BLUETOOTH_CONNECT] BluetoothConnectionBroadcastReceiver.onReceive", "action="+action);
//            Log.e("BluetoothConnectionBroadcastReceiver.onReceive", "[2] action="+action);

            BluetoothDevice _device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            PPApplicationStatic.logE("[BLUETOOTH_CONNECT] BluetoothConnectionBroadcastReceiver.onReceive", "device="+_device);
            if (_device != null)
                PPApplicationStatic.logE("[BLUETOOTH_CONNECT] BluetoothConnectionBroadcastReceiver.onReceive", "device="+_device.getName());

            //if (device == null)
            //    return;

            //final boolean connected = action.equals(BluetoothDevice.ACTION_ACL_CONNECTED);
            final String newName = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
            PPApplicationStatic.logE("[BLUETOOTH_CONNECT] BluetoothConnectionBroadcastReceiver.onReceive", "newName="+newName);

            // this is important, because ACTION_NAME_CHANGED is called very often
            if (action.equals(BluetoothDevice.ACTION_NAME_CHANGED)) {
                if (newName == null) {
                    return;
                }
                try {
                    if ((_device != null) && newName.equals(_device.getName())) {
                        return;
                    }
                } catch (SecurityException e) {
                    return;
                }
            }

            if (_device != null) {
                PPApplicationStatic.logE("[BLUETOOTH_CONNECT] BluetoothConnectionBroadcastReceiver.onReceive", "[2] device.name=" + _device.getName());
                PPApplicationStatic.logE("[BLUETOOTH_CONNECT] BluetoothConnectionBroadcastReceiver.onReceive", "[2] device.address=" + _device.getAddress());
            }

            final Context appContext = context.getApplicationContext();
            final WeakReference<BluetoothDevice> deviceWeakRef = new WeakReference<>(_device);
            Runnable runnable = () -> {
//                PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=BluetoothConnectionBroadcastReceiver.onReceive");
//                Log.e("BluetoothConnectionBroadcastReceiver.onReceive", "[2] start of executor");

                //Context appContext= appContextWeakRef.get();
                BluetoothDevice device = deviceWeakRef.get();

                if (/*(appContext != null) &&*/ (device != null)) {
                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WakelockTags.WAKELOCK_TAG_BluetoothConnectionBroadcastReceiver_onReceive);
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        if (EventStatic.getGlobalEventsRunning(appContext)) {

                            List<BluetoothDeviceData> connectedDevices = getConnectedDevices(appContext);

                            try {
                                switch (action) {
                                    case BluetoothDevice.ACTION_ACL_CONNECTED:
                                        addConnectedDevice(connectedDevices, device);
                                        break;
                                    case BluetoothDevice.ACTION_NAME_CHANGED:
                                        //noinspection ConstantConditions
                                        if (newName != null) {
                                            changeDeviceName(connectedDevices, device, newName);
                                        }
                                        break;
                                    default:
                                        removeConnectedDevice(connectedDevices, device);
                                        break;
                                }
                            } catch (Exception e) {
                                PPApplicationStatic.recordException(e);
                            }

                            saveConnectedDevices(connectedDevices, appContext);

                            callEventHandler(appContext);
                        }

                    } catch (Exception e) {
//                        PPApplicationStatic.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                        PPApplicationStatic.recordException(e);
                    } finally {
                        if ((wakeLock != null) && wakeLock.isHeld()) {
                            try {
                                wakeLock.release();
                            } catch (Exception ignored) {
                            }
                        }
                    }
                }
            };
            PPApplicationStatic.createEventsHandlerExecutor();
            PPApplication.eventsHandlerExecutor.submit(runnable);
        }

    }

    private static final String PREF_CONNECTED_DEVICES_COUNT = "count";
    private static final String PREF_CONNECTED_DEVICES_DEVICE = "device";

    @SuppressLint("MissingPermission")
    static List<BluetoothDeviceData> getConnectedDevices(Context context)
    {
//        PPApplicationStatic.logE("[SYNCHRONIZED] BluetoothConnectionBroadcastReceiver.getConnectedDevices", "PPApplication.bluetoothConnectionChangeStateMutex");
        synchronized (PPApplication.bluetoothConnectionChangeStateMutex) {
            //if (connectedDevices == null)
            //    connectedDevices = new ArrayList<>();
            //connectedDevices.clear();

            List<BluetoothDeviceData> connectedDevices = new ArrayList<>();

            SharedPreferences preferences = context.getSharedPreferences(PPApplication.BLUETOOTH_CONNECTED_DEVICES_PREFS_NAME, Context.MODE_PRIVATE);

            int count = preferences.getInt(PREF_CONNECTED_DEVICES_COUNT, 0);

            Gson gson = new Gson();

            int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();
            for (int i = 0; i < count; i++) {
                String json = preferences.getString(PREF_CONNECTED_DEVICES_DEVICE + i, "");
                if (!json.isEmpty()) {
                    BluetoothDeviceData device = gson.fromJson(json, BluetoothDeviceData.class);

                    //long bootTime = System.currentTimeMillis() - SystemClock.elapsedRealtime() - gmtOffset;
                    Calendar calendar = Calendar.getInstance();
                    long bootTime = calendar.getTimeInMillis() - SystemClock.elapsedRealtime() - gmtOffset;

                    if (device.timestamp >= bootTime) {
                        connectedDevices.add(device);
                    }
                }
            }

            return connectedDevices;
        }
    }

    static void saveConnectedDevices(List<BluetoothDeviceData> connectedDevices, Context context)
    {
//        PPApplicationStatic.logE("[SYNCHRONIZED] BluetoothConnectionBroadcastReceiver.saveConnectedDevices", "PPApplication.bluetoothConnectionChangeStateMutex");
        synchronized (PPApplication.bluetoothConnectionChangeStateMutex) {

            if (connectedDevices == null)
                connectedDevices = new ArrayList<>();

            SharedPreferences preferences = context.getSharedPreferences(PPApplication.BLUETOOTH_CONNECTED_DEVICES_PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();

            editor.clear();

            editor.putInt(PREF_CONNECTED_DEVICES_COUNT, connectedDevices.size());

            Gson gson = new Gson();

            int size = connectedDevices.size();
            for (int i = 0; i < size; i++) {
                String json = gson.toJson(connectedDevices.get(i));
                editor.putString(PREF_CONNECTED_DEVICES_DEVICE + i, json);
            }

            editor.apply();
        }
    }


    @SuppressLint("MissingPermission")
    private static void addConnectedDevice(List<BluetoothDeviceData> connectedDevices, BluetoothDevice device)
    {
//        PPApplicationStatic.logE("[SYNCHRONIZED] BluetoothConnectionBroadcastReceiver.addConnectedDevice", "PPApplication.bluetoothConnectionChangeStateMutex");
        if (device == null)
            return;

        //synchronized (PPApplication.bluetoothConnectionChangeStateMutex) {
            boolean found = false;
            if (device.getAddress() != null) {
                for (BluetoothDeviceData _device : connectedDevices) {
                    if ((_device.getAddress() != null) &&
                            _device.getAddress().equals(device.getAddress())) {
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                if (device.getName() != null) {
                    for (BluetoothDeviceData _device : connectedDevices) {
                        if ((_device.getName() != null) &&
                                _device.getName().equalsIgnoreCase(device.getName())) {
                            found = true;
                            break;
                        }
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
        //}
    }

    @SuppressLint("MissingPermission")
    private static void removeConnectedDevice(List<BluetoothDeviceData> connectedDevices, BluetoothDevice device)
    {
//        PPApplicationStatic.logE("[SYNCHRONIZED] BluetoothConnectionBroadcastReceiver.removeConnectedDevice", "PPApplication.bluetoothConnectionChangeStateMutex");
        if (device == null)
            return;

        //synchronized (PPApplication.bluetoothConnectionChangeStateMutex) {
            //int index = 0;
            BluetoothDeviceData deviceToRemove = null;
            boolean found = false;
            if (device.getAddress() != null) {
                for (BluetoothDeviceData _device : connectedDevices) {
                    if ((_device.getAddress() != null) &&
                            _device.getAddress().equals(device.getAddress())) {
                        found = true;
                        deviceToRemove = _device;
                        break;
                    }
                    //++index;
                }
            }
            if (!found) {
                //index = 0;
                if (device.getName() != null) {
                    for (BluetoothDeviceData _device : connectedDevices) {
                        if ((_device.getName() != null) &&
                                _device.getName().equalsIgnoreCase(device.getName())) {
                            found = true;
                            deviceToRemove = _device;
                            break;
                        }
                        //++index;
                    }
                }
            }
//            PPApplicationStatic.logE("[IN_BROADCAST] BluetoothConnectionBroadcastReceiver.removeConnectedDevice", "device="+device.getName());
            if (found)
                //connectedDevices.remove(index);
                connectedDevices.remove(deviceToRemove);
        //}
    }

    static void clearConnectedDevices(List<BluetoothDeviceData> connectedDevices/*Context context, boolean onlyOld*/)
    {
        //if (onlyOld) {
        //    getConnectedDevices(context);
        //}

//        PPApplicationStatic.logE("[SYNCHRONIZED] BluetoothConnectionBroadcastReceiver.clearConnectedDevices", "PPApplication.bluetoothConnectionChangeStateMutex");
        //synchronized (PPApplication.bluetoothConnectionChangeStateMutex) {
            if (connectedDevices != null) {
                /*if (onlyOld) {
                    int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();
                    for (Iterator<BluetoothDeviceData> it = connectedDevices.iterator(); it.hasNext(); ) {
                        BluetoothDeviceData device = it.next();
                        //long bootTime = System.currentTimeMillis() - SystemClock.elapsedRealtime() - gmtOffset;
                        Calendar calendar = Calendar.getInstance();
                        long bootTime = calendar.getTimeInMillis() - SystemClock.elapsedRealtime() - gmtOffset;
                        if (device.timestamp < bootTime)
                            //connectedDevices.remove(device);
                            it.remove();
                    }
                }
                else*/
                    connectedDevices.clear();
            }
        //}
    }


    @SuppressLint("MissingPermission")
    private static void changeDeviceName(List<BluetoothDeviceData> connectedDevices, BluetoothDevice device, String deviceName)
    {
//        PPApplicationStatic.logE("[SYNCHRONIZED] BluetoothConnectionBroadcastReceiver.changeDeviceName", "PPApplication.bluetoothConnectionChangeStateMutex");
        if (device == null)
            return;

        //synchronized (PPApplication.bluetoothConnectionChangeStateMutex) {
            boolean found = false;
            if (device.getAddress() != null) {
                for (BluetoothDeviceData _device : connectedDevices) {
                    if ((_device.getAddress() != null) &&
                            _device.getAddress().equals(device.getAddress()) &&
                            !deviceName.isEmpty()) {
                        _device.setName(deviceName);
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                if (device.getName() != null) {
                    for (BluetoothDeviceData _device : connectedDevices) {
                        if ((_device.getName() != null) &&
                                _device.getName().equalsIgnoreCase(device.getName()) &&
                                !deviceName.isEmpty()) {
                            _device.setName(deviceName);
                            break;
                        }
                    }
                }
            }
        //}
    }

    static void addConnectedDeviceData(List<BluetoothDeviceData> connectedDevices, List<BluetoothDeviceData> connectedDevicesToAdd)
    {
//        PPApplicationStatic.logE("[SYNCHRONIZED] BluetoothConnectionBroadcastReceiver.addConnectedDeviceData", "PPApplication.bluetoothConnectionChangeStateMutex");
        //synchronized (PPApplication.bluetoothConnectionChangeStateMutex) {
            for (BluetoothDeviceData newDevice : connectedDevicesToAdd) {
                boolean found = false;

                String newName = newDevice.getName();
                if (!newName.isEmpty()) {
                    // do not add device without name

                    String newAddress = newDevice.getAddress();
                    if (!newAddress.isEmpty()) {
                        for (BluetoothDeviceData connectedDevice : connectedDevices) {
                            if ((!connectedDevice.getAddress().isEmpty()) &&
                                    connectedDevice.getAddress().equals(newAddress)) {
                                found = true;
                                break;
                            }
                        }
                    }
                    if (!found) {
                        for (BluetoothDeviceData connectedDevice : connectedDevices) {
                            String connectedName = connectedDevice.getName();
                            if ((!connectedName.isEmpty()) &&
                                    connectedName.equalsIgnoreCase(newName)) {
                                found = true;
                                break;
                            }
                        }
                    }
                    if (!found) {
                        connectedDevices.add(newDevice);
                    }
                }
            }
        //}
    }

    static boolean isBluetoothConnected(List<BluetoothDeviceData> connectedDevices, BluetoothDeviceData deviceData, String sensorDeviceName)
    {
//        PPApplicationStatic.logE("[SYNCHRONIZED] BluetoothConnectionBroadcastReceiver.isBluetoothConnected", "PPApplication.bluetoothConnectionChangeStateMutex");
        //synchronized (PPApplication.bluetoothConnectionChangeStateMutex) {
            if ((deviceData == null) && sensorDeviceName.isEmpty()) {
                // is device connected to any external bluetooth device ???

                return (connectedDevices != null) && (!connectedDevices.isEmpty());
            }
            else {
                if (connectedDevices != null) {
                    if ((deviceData != null) && sensorDeviceName.isEmpty()) {
                        // is deviceData connected ???

                        // deviceData is from bounded devices and bounded devices are with address
                        String deviceDataName = deviceData.getName().trim().toUpperCase();
                        if (!deviceDataName.isEmpty()) {
                            // device without empty name is not supported

                            boolean found = false;

                            String deviceDataAddress = deviceData.getAddress();
                            if (!deviceDataAddress.isEmpty()) {
                                for (BluetoothDeviceData connectedDevice : connectedDevices) {
                                    String connectedDeviceAddress = connectedDevice.getAddress();
                                    if ((!connectedDeviceAddress.isEmpty()) &&
                                            connectedDeviceAddress.equals(deviceDataAddress)) {
                                        found = true;
                                        break;
                                    }
                                }
                            }
                            if (!found) {
                                /*for (BluetoothDeviceData connectedDevice : connectedDevices) {
                                    String connectedName = connectedDevice.getName().trim().toUpperCase();
                                    if ((!connectedName.isEmpty()) &&
                                            connectedName.equalsIgnoreCase(deviceDataName)) {
                                        found = true;
                                        break;
                                    }
                                }*/
                                for (BluetoothDeviceData connectedDevice : connectedDevices) {
                                    String connectedName = connectedDevice.getName().trim().toUpperCase();
                                    if ((!connectedName.isEmpty()) &&
                                            Wildcard.match(connectedName, deviceDataName, '_', '%', true)) {
                                        found = true;
                                        break;
                                    }
                                }
                            }

                            return found;
                        }
                    }
                    else {
                        // is sensorDeviceName connected ???

                        // get connected device by address
                        // deviceData is from bounded devices and bounded devices are with address
                        BluetoothDeviceData connectedDevice = null;
                        if (deviceData != null) {
                            String deviceDataAddress = deviceData.getAddress();
                            if (!deviceDataAddress.isEmpty()) {
                                for (BluetoothDeviceData _connectedDevice : connectedDevices) {
                                    String _connectedDeviceAddress = _connectedDevice.getAddress();
                                    if ((!_connectedDeviceAddress.isEmpty()) &&
                                            _connectedDeviceAddress.equals(deviceDataAddress)) {
                                        connectedDevice = _connectedDevice;
                                        break;
                                    }
                                }
                            }
                        }

                        //noinspection IfStatementWithIdenticalBranches
                        if (connectedDevice != null) {
                            // configured device in system is connected
                            String sensorName = sensorDeviceName.trim().toUpperCase();
                            if (!sensorName.isEmpty()) {
                                String connectedName = connectedDevice.getName().trim().toUpperCase();
                                if ((!connectedName.isEmpty()) &&
                                        Wildcard.match(connectedName, sensorName, '_', '%', true)) {
                                    // configured connected device in system is sensorDeviceName
                                    return true;
                                }
                            }
                        } else {
                            // check only by device name
                            String sensorName = sensorDeviceName.trim().toUpperCase();
                            if (!sensorName.isEmpty()) {
                                for (BluetoothDeviceData _connectedDevice : connectedDevices) {
                                    String connectedName = _connectedDevice.getName().trim().toUpperCase();
                                    if ((!connectedName.isEmpty()) &&
                                            Wildcard.match(connectedName, sensorName, '_', '%', true)) {
                                        // connected device in system is sensorDeviceName
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                }
                return false;
            }
        //}
    }

    private void callEventHandler(final Context appContext) {
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

/*    private static abstract class PPHandlerThreadRunnable implements Runnable {

        final WeakReference<Context> appContextWeakRef;
        final WeakReference<BluetoothDevice> deviceWeakRef;

        PPHandlerThreadRunnable(Context appContext,
                                       BluetoothDevice device) {
            this.appContextWeakRef = new WeakReference<>(appContext);
            this.deviceWeakRef = new WeakReference<>(device);
        }

    }*/

}
