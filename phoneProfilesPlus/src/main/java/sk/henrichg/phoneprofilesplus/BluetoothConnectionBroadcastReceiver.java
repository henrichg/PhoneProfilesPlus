package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;

import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static android.content.Context.POWER_SERVICE;

public class BluetoothConnectionBroadcastReceiver extends BroadcastReceiver {

    private static List<BluetoothDeviceData> connectedDevices = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### BluetoothConnectionBroadcastReceiver.onReceive", "xxx");

        CallsCounter.logCounter(context, "BluetoothConnectionBroadcastReceiver.onReceive", "BluetoothConnectionBroadcastReceiver_onReceive");

        final Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(appContext, true))
            // application is not started
            return;

        if (intent == null)
            return;

        /*BluetoothJob.startForConnectionBroadcast(appContext, intent.getAction(),
                intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE),
                intent.getStringExtra(BluetoothDevice.EXTRA_NAME));*/

        final String action = intent.getAction();
        if (action == null)
            return;

        PPApplication.logE("BluetoothConnectionBroadcastReceiver.onReceive", "action=" + action);

        if (action.equals(BluetoothDevice.ACTION_ACL_CONNECTED) ||
                action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED) ||
                action.equals(BluetoothDevice.ACTION_NAME_CHANGED)/* ||
                action.equals(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED)*/) {
            // BluetoothConnectionBroadcastReceiver

            final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            final boolean connected = action.equals(BluetoothDevice.ACTION_ACL_CONNECTED);
            final String newName = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);

            PhoneProfilesService.startHandlerThread();
            final Handler handler = new Handler(PhoneProfilesService.handlerThread.getLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {

                    PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "BluetoothConnectionBroadcastReceiver.onReceive");
                        wakeLock.acquire(10 * 60 * 1000);
                    }

                    getConnectedDevices(appContext);

                    if (device != null) {

                        try {
                            if (!action.equals(BluetoothDevice.ACTION_NAME_CHANGED)) {
                                PPApplication.logE("BluetoothConnectionBroadcastReceiver.onReceive", "connected=" + connected);
                                PPApplication.logE("BluetoothConnectionBroadcastReceiver.onReceive", "device.getName()=" + device.getName());
                                PPApplication.logE("BluetoothConnectionBroadcastReceiver.onReceive", "device.getAddress()=" + device.getAddress());
                            }

                            if (action.equals(BluetoothDevice.ACTION_ACL_CONNECTED))
                                addConnectedDevice(device);
                            else if (action.equals(BluetoothDevice.ACTION_NAME_CHANGED)) {
                                if (newName != null)
                                    changeDeviceName(device, newName);
                            } else
                                removeConnectedDevice(device);
                        } catch (Exception ignored) {
                        }

                        saveConnectedDevices(appContext);

                        /*SharedPreferences preferences = appContext.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
                        int lastState = preferences.getInt(PPApplication.PREF_EVENT_BLUETOOTH_LAST_STATE, -1);
                        int currState = -1;
                        if (connected)
                            currState = 1;
                        if (!connected)
                            currState = 0;
                        Editor editor = preferences.edit();
                        editor.putInt(PPApplication.PREF_EVENT_BLUETOOTH_LAST_STATE, currState);
                        editor.commit();*/

                        if (Event.getGlobalEventsRunning(appContext)) {

                            //if (lastState != currState)
                            //{
                            PPApplication.logE("@@@ BluetoothConnectionBroadcastReceiver.onReceive", "connected=" + connected);

                            if (!((BluetoothScanJob.getScanRequest(appContext)) ||
                                    (BluetoothScanJob.getLEScanRequest(appContext)) ||
                                    (BluetoothScanJob.getWaitForResults(appContext)) ||
                                    (BluetoothScanJob.getWaitForLEResults(appContext)) ||
                                    (BluetoothScanJob.getBluetoothEnabledForScan(appContext)))) {
                                // bluetooth is not scanned

                                /*DataWrapper dataWrapper = new DataWrapper(appContext, false, false, 0);
                                boolean bluetoothEventsExists = dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_BLUETOOTHCONNECTED) > 0;
                                dataWrapper.invalidateDataWrapper();

                                if (bluetoothEventsExists)
                                {
                                    PPApplication.logE("@@@ BluetoothJob.onRunJob","BluetoothConnectionBroadcastReceiver: bluetoothEventsExists="+bluetoothEventsExists);
                                */
                                // start events handler
                                EventsHandler eventsHandler = new EventsHandler(appContext);
                                eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_BLUETOOTH_CONNECTION, false);
                                //}
                            }
                            //}
                        }
                    }

                    if (wakeLock != null)
                        wakeLock.release();
                }
            });
        }

    }

    private static final String CONNECTED_DEVICES_COUNT_PREF = "count";
    private static final String CONNECTED_DEVICES_DEVICE_PREF = "device";

    private static void getConnectedDevices(Context context)
    {
        synchronized (PPApplication.bluetoothConnectionChangeStateMutex) {

            if (connectedDevices == null)
                connectedDevices = new ArrayList<>();

            connectedDevices.clear();

            SharedPreferences preferences = context.getSharedPreferences(PPApplication.BLUETOOTH_CONNECTED_DEVICES_PREFS_NAME, Context.MODE_PRIVATE);

            int count = preferences.getInt(CONNECTED_DEVICES_COUNT_PREF, 0);

            Gson gson = new Gson();

            int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();
            for (int i = 0; i < count; i++) {
                String json = preferences.getString(CONNECTED_DEVICES_DEVICE_PREF + i, "");
                if (!json.isEmpty()) {
                    BluetoothDeviceData device = gson.fromJson(json, BluetoothDeviceData.class);

                    @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                    PPApplication.logE("BluetoothConnectionBroadcastReceiver.getConnectedDevices","device.name="+device.name);
                    PPApplication.logE("BluetoothConnectionBroadcastReceiver.getConnectedDevices", "device.timestamp="+sdf.format(device.timestamp));
                    //long bootTime = System.currentTimeMillis() - SystemClock.elapsedRealtime() - gmtOffset;
                    Calendar calendar = Calendar.getInstance();
                    long bootTime = calendar.getTimeInMillis() - SystemClock.elapsedRealtime() - gmtOffset;
                    PPApplication.logE("BluetoothConnectionBroadcastReceiver.getConnectedDevices", "bootTime="+sdf.format(bootTime));

                    if (device.timestamp >= bootTime) {
                        PPApplication.logE("BluetoothConnectionBroadcastReceiver.getConnectedDevices", "added");
                        connectedDevices.add(device);
                    }
                    else
                        PPApplication.logE("BluetoothConnectionBroadcastReceiver.getConnectedDevices", "not added");
                }
            }

            PPApplication.logE("BluetoothConnectionBroadcastReceiver.getConnectedDevices", "connectedDevices.size()=" + connectedDevices.size());
        }
    }

    static void saveConnectedDevices(Context context)
    {
        synchronized (PPApplication.bluetoothConnectionChangeStateMutex) {

            if (connectedDevices == null)
                connectedDevices = new ArrayList<>();

            SharedPreferences preferences = context.getSharedPreferences(PPApplication.BLUETOOTH_CONNECTED_DEVICES_PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();

            editor.clear();

            editor.putInt(CONNECTED_DEVICES_COUNT_PREF, connectedDevices.size());

            Gson gson = new Gson();

            for (int i = 0; i < connectedDevices.size(); i++) {
                String json = gson.toJson(connectedDevices.get(i));
                editor.putString(CONNECTED_DEVICES_DEVICE_PREF + i, json);
            }

            editor.apply();
        }
    }

    private void addConnectedDevice(BluetoothDevice device)
    {
        synchronized (PPApplication.bluetoothConnectionChangeStateMutex) {
            PPApplication.logE("BluetoothConnectionBroadcastReceiver.addConnectedDevice","device.name="+device.getName());
            PPApplication.logE("BluetoothConnectionBroadcastReceiver.addConnectedDevice","device.address="+device.getAddress());
            boolean found = false;
            for (BluetoothDeviceData _device : connectedDevices) {
                if (_device.address.equals(device.getAddress())) {
                    found = true;
                    break;
                }
            }
            PPApplication.logE("BluetoothConnectionBroadcastReceiver.addConnectedDevice","found="+found);
            if (!found) {
                int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();
                Calendar now = Calendar.getInstance();
                long timestamp = now.getTimeInMillis() - gmtOffset;
                connectedDevices.add(new BluetoothDeviceData(device.getName(), device.getAddress(),
                        BluetoothScanJob.getBluetoothType(device), false, timestamp));
            }
        }
    }

    private void removeConnectedDevice(BluetoothDevice device)
    {
        synchronized (PPApplication.bluetoothConnectionChangeStateMutex) {
            PPApplication.logE("BluetoothConnectionBroadcastReceiver.removeConnectedDevice","device.name="+device.getName());
            PPApplication.logE("BluetoothConnectionBroadcastReceiver.removeConnectedDevice","device.address="+device.getAddress());
            int index = 0;
            boolean found = false;
            for (BluetoothDeviceData _device : connectedDevices) {
                if (_device.address.equals(device.getAddress())) {
                    found = true;
                    break;
                }
                ++index;
            }
            PPApplication.logE("BluetoothConnectionBroadcastReceiver.removeConnectedDevice","found="+found);
            if (found)
                connectedDevices.remove(index);
        }
    }

    static void clearConnectedDevices(Context context, boolean onlyOld)
    {
        PPApplication.logE("BluetoothConnectionBroadcastReceiver.clearConnectedDevices","onlyOld="+onlyOld);

        if (onlyOld) {
            getConnectedDevices(context);
        }

        synchronized (PPApplication.bluetoothConnectionChangeStateMutex) {
            if (connectedDevices != null) {
                if (onlyOld) {
                    int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();
                    for (BluetoothDeviceData device : connectedDevices) {
                        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                        PPApplication.logE("BluetoothConnectionBroadcastReceiver.clearConnectedDevices","device.name="+device.name);
                        PPApplication.logE("BluetoothConnectionBroadcastReceiver.clearConnectedDevices", "device.timestamp="+sdf.format(device.timestamp));
                        //long bootTime = System.currentTimeMillis() - SystemClock.elapsedRealtime() - gmtOffset;
                        Calendar calendar = Calendar.getInstance();
                        long bootTime = calendar.getTimeInMillis() - SystemClock.elapsedRealtime() - gmtOffset;
                        PPApplication.logE("BluetoothConnectionBroadcastReceiver.clearConnectedDevices", "bootTime="+sdf.format(bootTime));
                        if (device.timestamp < bootTime)
                            connectedDevices.remove(device);
                    }
                }
                else
                    connectedDevices.clear();
            }
        }
    }

    private void changeDeviceName(BluetoothDevice device, String deviceName)
    {
        synchronized (PPApplication.bluetoothConnectionChangeStateMutex) {
            for (BluetoothDeviceData _device : connectedDevices) {
                if (_device.address.equals(device.getAddress()) && !deviceName.isEmpty()) {
                    _device.setName(deviceName);
                    break;
                }
            }
        }
    }

    static boolean isBluetoothConnected(Context context, String adapterName)
    {
        getConnectedDevices(context);

        synchronized (PPApplication.bluetoothConnectionChangeStateMutex) {
            if (adapterName.isEmpty())
                return (connectedDevices != null) && (connectedDevices.size() > 0);
            else {
                if (connectedDevices != null) {
                    for (BluetoothDeviceData _device : connectedDevices) {
                        String device = _device.getName().toUpperCase();
                        String _adapterName = adapterName.toUpperCase();
                        if (Wildcard.match(device, _adapterName, '_', '%', true)) {
                            return true;
                        }
                    }
                }
                return false;
            }
        }
    }

}
