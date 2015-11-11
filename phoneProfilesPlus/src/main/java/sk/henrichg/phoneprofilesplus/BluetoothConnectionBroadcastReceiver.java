package sk.henrichg.phoneprofilesplus;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class BluetoothConnectionBroadcastReceiver extends WakefulBroadcastReceiver {

    public static final String BROADCAST_RECEIVER_TYPE = "bluetoothConnection";

    private static List<BluetoothDeviceData> connectedDevices = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        GlobalData.logE("##### BluetoothConnectionBroadcastReceiver.onReceive", "xxx");

        getConnectedDevices(context);

        String action = intent.getAction();
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

        if (action.equals(BluetoothDevice.ACTION_ACL_CONNECTED) ||
            action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED) ||
            action.equals(BluetoothDevice.ACTION_NAME_CHANGED)/* ||
            action.equals(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED)*/)
        {
            boolean connected = action.equals(BluetoothDevice.ACTION_ACL_CONNECTED);

            if (action.equals(BluetoothDevice.ACTION_ACL_CONNECTED))
                addConnectedDevice(device);
            else
            if (action.equals(BluetoothDevice.ACTION_NAME_CHANGED))
                changeDeviceName(device, intent.getStringExtra(BluetoothDevice.EXTRA_NAME));
            else
                removeConnectedDevice(device);

            saveConnectedDevices(context);

            if (!GlobalData.getApplicationStarted(context))
                // application is not started
                return;

            GlobalData.loadPreferences(context);

            /*SharedPreferences preferences = context.getSharedPreferences(GlobalData.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
            int lastState = preferences.getInt(GlobalData.PREF_EVENT_BLUETOOTH_LAST_STATE, -1);
            int currState = -1;
            if (connected)
                currState = 1;
            if (!connected)
                currState = 0;
            Editor editor = preferences.edit();
            editor.putInt(GlobalData.PREF_EVENT_BLUETOOTH_LAST_STATE, currState);
            editor.commit();*/

            if (GlobalData.getGlobalEventsRuning(context))
            {

                //if (lastState != currState)
                //{
                    GlobalData.logE("@@@ BluetoothConnectionBroadcastReceiver.onReceive","connected"+connected);

                    if (!((BluetoothScanAlarmBroadcastReceiver.getScanRequest(context)) ||
                         (BluetoothScanAlarmBroadcastReceiver.getLEScanRequest(context)) ||
                         (BluetoothScanAlarmBroadcastReceiver.getWaitForResults(context)) ||
                         (BluetoothScanAlarmBroadcastReceiver.getWaitForLEResults(context)) ||
                         (BluetoothScanAlarmBroadcastReceiver.getBluetoothEnabledForScan(context))))
                    {
                        // bluetooth is not scanned

                        DataWrapper dataWrapper = new DataWrapper(context, false, false, 0);
                        boolean bluetoothEventsExists = dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_BLUETOOTHCONNECTED) > 0;
                        dataWrapper.invalidateDataWrapper();

                        if (bluetoothEventsExists)
                        {
                            GlobalData.logE("@@@ BluetoothConnectionBroadcastReceiver.onReceive","bluetoothEventsExists="+bluetoothEventsExists);

                            // start service
                            Intent eventsServiceIntent = new Intent(context, EventsService.class);
                            eventsServiceIntent.putExtra(GlobalData.EXTRA_BROADCAST_RECEIVER_TYPE, BROADCAST_RECEIVER_TYPE);
                            startWakefulService(context, eventsServiceIntent);
                        }
                    }

                //}
            }

            //if ((!connected) && (lastState != currState))
            /*if (!connected)
            {
                BluetoothScanAlarmBroadcastReceiver.stopScan(context);
            }*/

        }
    }

    private static final String CONNECTED_DEVICES_COUNT_PREF = "count";
    private static final String CONNECTED_DEVICES_DEVICE_PREF = "device";

    private static void getConnectedDevices(Context context)
    {
        synchronized (GlobalData.bluetoothConnectionChangeStateMutex) {

            if (connectedDevices == null)
                connectedDevices = new ArrayList<BluetoothDeviceData>();

            connectedDevices.clear();

            SharedPreferences preferences = context.getSharedPreferences(GlobalData.BLUETOOTH_CONNECTED_DEVICES_PREFS_NAME, Context.MODE_PRIVATE);

            int count = preferences.getInt(CONNECTED_DEVICES_COUNT_PREF, 0);

            Gson gson = new Gson();

            for (int i = 0; i < count; i++) {
                String json = preferences.getString(CONNECTED_DEVICES_DEVICE_PREF + i, "");
                if (!json.isEmpty()) {
                    BluetoothDeviceData device = gson.fromJson(json, BluetoothDeviceData.class);
                    connectedDevices.add(device);
                }
            }
        }
    }

    private static void saveConnectedDevices(Context context)
    {
        synchronized (GlobalData.bluetoothConnectionChangeStateMutex) {

            if (connectedDevices == null)
                connectedDevices = new ArrayList<BluetoothDeviceData>();

            SharedPreferences preferences = context.getSharedPreferences(GlobalData.BLUETOOTH_CONNECTED_DEVICES_PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();

            editor.clear();

            editor.putInt(CONNECTED_DEVICES_COUNT_PREF, connectedDevices.size());

            Gson gson = new Gson();

            for (int i = 0; i < connectedDevices.size(); i++) {
                String json = gson.toJson(connectedDevices.get(i));
                editor.putString(CONNECTED_DEVICES_DEVICE_PREF + i, json);
            }

            editor.commit();
        }
    }

    private void addConnectedDevice(BluetoothDevice device)
    {
        synchronized (GlobalData.bluetoothConnectionChangeStateMutex) {

            boolean found = false;
            for (BluetoothDeviceData _device : connectedDevices) {
                if (_device.address.equals(device.getAddress())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                connectedDevices.add(new BluetoothDeviceData(device.getName(), device.getAddress()));
            }
        }
    }

    private void removeConnectedDevice(BluetoothDevice device)
    {
        synchronized (GlobalData.bluetoothConnectionChangeStateMutex) {
            int index = 0;
            boolean found = false;
            for (BluetoothDeviceData _device : connectedDevices) {
                if (_device.address.equals(device.getAddress())) {
                    found = true;
                    break;
                }
                ++index;
            }
            if (found)
                connectedDevices.remove(index);
        }
    }

    private void changeDeviceName(BluetoothDevice device, String deviceName)
    {
        synchronized (GlobalData.bluetoothConnectionChangeStateMutex) {
            for (BluetoothDeviceData _device : connectedDevices) {
                if (_device.address.equals(device.getAddress()) && !deviceName.isEmpty()) {
                    _device.setName(deviceName);
                    break;
                }
            }
        }
    }

    public static boolean isBluetoothConnected(Context context, String adapterName)
    {
        getConnectedDevices(context);

        synchronized (GlobalData.bluetoothConnectionChangeStateMutex) {
            if (adapterName.isEmpty())
                return (connectedDevices != null) && (connectedDevices.size() > 0);
            else {
                if (connectedDevices != null) {
                    for (BluetoothDeviceData _device : connectedDevices) {
                        String device = _device.getName().toUpperCase();
                        String _adapterName = adapterName.toUpperCase();
                        if (Wildcard.match(device, _adapterName, '_', '%')) {
                            return true;
                        }
                    }
                }
                return false;
            }
        }
    }

    public static boolean isAdapterNameScanned(DataWrapper dataWrapper, int connectionType)
    {
        if (isBluetoothConnected(dataWrapper.context, ""))
        {
            synchronized (GlobalData.bluetoothConnectionChangeStateMutex) {
                if (connectedDevices != null) {
                    for (BluetoothDeviceData _device : connectedDevices) {
                        if (dataWrapper.getDatabaseHandler().isBluetoothAdapterNameScanned(_device.getName(), connectionType))
                            return true;
                    }
                }
                return false;
            }
        }
        else
            return false;
    }

}
