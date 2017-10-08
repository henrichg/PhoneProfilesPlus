package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class BluetoothService extends WakefulIntentService {

    private static List<BluetoothDeviceData> connectedDevices = null;

    public BluetoothService() {
        super("BluetoothService");
    }

    @Override
    protected void doWakefulWork(Intent intent) {
        CallsCounter.logCounter(getApplicationContext(), "BluetoothService.doWakefulWork", "BluetoothService_doWakefulWork");

        if (intent != null) {
            Context appContext = getApplicationContext();

            String action = intent.getAction();

            if (action.equals(BluetoothDevice.ACTION_ACL_CONNECTED) ||
                action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED) ||
                action.equals(BluetoothDevice.ACTION_NAME_CHANGED)/* ||
                action.equals(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED)*/) {
                // BluetoothConnectionBroadcastReceiver

                getConnectedDevices(appContext);

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (device != null) {

                    boolean connected = action.equals(BluetoothDevice.ACTION_ACL_CONNECTED);

                    try {
                        if (!action.equals(BluetoothDevice.ACTION_NAME_CHANGED)) {
                            PPApplication.logE("BluetoothService.doWakefulWork", "BluetoothConnectionBroadcastReceiver: connected=" + connected);
                            PPApplication.logE("BluetoothService.doWakefulWork", "BluetoothConnectionBroadcastReceiver: device.getName()=" + device.getName());
                            PPApplication.logE("BluetoothService.doWakefulWork", "BluetoothConnectionBroadcastReceiver: device.getAddress()=" + device.getAddress());
                        }

                        if (action.equals(BluetoothDevice.ACTION_ACL_CONNECTED))
                            addConnectedDevice(device);
                        else if (action.equals(BluetoothDevice.ACTION_NAME_CHANGED)) {
                            String deviceName = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
                            if (deviceName != null)
                                changeDeviceName(device, deviceName);
                        } else
                            removeConnectedDevice(device);
                    } catch (Exception ignored) {}

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
                        PPApplication.logE("@@@ BluetoothService.doWakefulWork", "BluetoothConnectionBroadcastReceiver: connected=" + connected);

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
                                PPApplication.logE("@@@ BluetoothService.doWakefulWork","BluetoothConnectionBroadcastReceiver: bluetoothEventsExists="+bluetoothEventsExists);
                            */
                            // start events handler
                            EventsHandler eventsHandler = new EventsHandler(appContext);
                            eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_BLUETOOTH_CONNECTION, false);
                            //}
                        }
                        //}
                    }
                }
            }
            else
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                // BluetoothStateChangedBroadcastReceiver

                int bluetoothState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                // remove connected devices list
                if (bluetoothState == BluetoothAdapter.STATE_OFF) {
                    clearConnectedDevices(appContext, false);
                    saveConnectedDevices(appContext);
                }

                if (Event.getGlobalEventsRunning(appContext))
                {
                    PPApplication.logE("@@@ BluetoothService.doWakefulWork","BluetoothStateChangedBroadcastReceiver: state="+bluetoothState);

                    if ((bluetoothState == BluetoothAdapter.STATE_ON) || (bluetoothState == BluetoothAdapter.STATE_OFF)) {

                        if (bluetoothState == BluetoothAdapter.STATE_ON)
                        {
                            //if ((!dataWrapper.getIsManualProfileActivation()) || PPApplication.getForceOneBluetoothScan(appContext))
                            //{
                            if (BluetoothScanJob.getScanRequest(appContext))
                            {
                                PPApplication.logE("@@@ BluetoothService.doWakefulWork", "BluetoothStateChangedBroadcastReceiver: start classic scan");
                                BluetoothScanJob.startCLScan(appContext);
                            }
                            else
                            if (BluetoothScanJob.getLEScanRequest(appContext))
                            {
                                PPApplication.logE("@@@ BluetoothService.doWakefulWork", "BluetoothStateChangedBroadcastReceiver: start LE scan");
                                BluetoothScanJob.startLEScan(appContext);
                            }
                            else
                            if (!(BluetoothScanJob.getWaitForResults(appContext) ||
                                    BluetoothScanJob.getWaitForLEResults(appContext)))
                            {
                                // refresh bounded devices
                                BluetoothScanJob.fillBoundedDevicesList(appContext);
                            }
                            //}
                        }

                        if (!((BluetoothScanJob.getScanRequest(appContext)) ||
                                (BluetoothScanJob.getLEScanRequest(appContext)) ||
                                (BluetoothScanJob.getWaitForResults(appContext)) ||
                                (BluetoothScanJob.getWaitForLEResults(appContext)) ||
                                (BluetoothScanJob.getBluetoothEnabledForScan(appContext)))) {
                            // required for Bluetooth ConnectionType="Not connected"

                            //if ((bluetoothState == BluetoothAdapter.STATE_ON) || (bluetoothState == BluetoothAdapter.STATE_OFF)) {

                            // start events handler
                            EventsHandler eventsHandler = new EventsHandler(appContext);
                            eventsHandler.setEventRadioSwitchParameters(EventPreferencesRadioSwitch.RADIO_TYPE_BLUETOOTH, bluetoothState == BluetoothAdapter.STATE_ON);
                            eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_RADIO_SWITCH, false);

                            //}

                            /*DataWrapper dataWrapper = new DataWrapper(appContext, false, false, 0);
                            boolean bluetoothEventsExists = dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_BLUETOOTHCONNECTED) > 0;
                            dataWrapper.invalidateDataWrapper();

                            if (bluetoothEventsExists) {
                                PPApplication.logE("@@@ BluetoothService.doWakefulWork", "BluetoothStateChangedBroadcastReceiver: bluetoothEventsExists=" + bluetoothEventsExists);
                            */

                            // start events handler
                            eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_BLUETOOTH_STATE, false);

                            //}
                        }

                    }
                }
            }
            else
            if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED) ||
                action.equals(BluetoothDevice.ACTION_FOUND) ||
                action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                // BluetoothScanBroadcastReceiver

                if (BluetoothScanJob.bluetooth == null)
                    BluetoothScanJob.bluetooth = BluetoothScanJob.getBluetoothAdapter(appContext);

                int forceOneScan = Scanner.getForceOneBluetoothScan(appContext);

                if (Event.getGlobalEventsRunning(appContext) || (forceOneScan == Scanner.FORCE_ONE_SCAN_FROM_PREF_DIALOG))
                {

                    boolean scanStarted = (BluetoothScanJob.getWaitForResults(appContext));

                    if (scanStarted)
                    {
                        PPApplication.logE("@@@ BluetoothService.doWakefulWork","BluetoothScanBroadcastReceiver: action="+action);

                        if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action))
                        {
                            // may be not invoked if not any BT is around

                            if (!Scanner.bluetoothDiscoveryStarted) {
                                Scanner.bluetoothDiscoveryStarted = true;
                                BluetoothScanJob.fillBoundedDevicesList(appContext);
                            }
                        }
                        else if (BluetoothDevice.ACTION_FOUND.equals(action))
                        {
                            // When discovery finds a device

                            if (!Scanner.bluetoothDiscoveryStarted) {
                                Scanner.bluetoothDiscoveryStarted = true;
                                BluetoothScanJob.fillBoundedDevicesList(appContext);
                            }

                            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                            String btNameD = device.getName();
                            String btNameE = "";
                            String btName = btNameD;
                            if (intent.hasExtra(BluetoothDevice.EXTRA_NAME)) {
                                btNameE = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
                                btName = btNameE;
                            }

                            PPApplication.logE("@@@ BluetoothService.doWakefulWork","BluetoothScanBroadcastReceiver: deviceName_d="+btNameD);
                            PPApplication.logE("@@@ BluetoothService.doWakefulWork","BluetoothScanBroadcastReceiver: deviceName_e="+btNameE);
                            PPApplication.logE("@@@ BluetoothService.doWakefulWork","BluetoothScanBroadcastReceiver: deviceAddress="+device.getAddress());

                            if (Scanner.tmpBluetoothScanResults == null)
                                Scanner.tmpBluetoothScanResults = new ArrayList<>();

                            boolean found = false;
                            for (BluetoothDeviceData _device : Scanner.tmpBluetoothScanResults)
                            {
                                if (_device.address.equals(device.getAddress()))
                                {
                                    found = true;
                                    break;
                                }
                            }
                            if (!found)
                            {
                                Scanner.tmpBluetoothScanResults.add(new BluetoothDeviceData(btName, device.getAddress(),
                                        BluetoothScanJob.getBluetoothType(device), false, 0));
                            }
                        }
                        else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
                        {
                            if (!Scanner.bluetoothDiscoveryStarted) {
                                Scanner.bluetoothDiscoveryStarted = true;
                                BluetoothScanJob.fillBoundedDevicesList(appContext);
                            }

                            BluetoothScanJob.finishScan(appContext);
                        }

                    }

                }
            }
            else
            if (action.equals("BluetoothLEScanBroadcastReceiver")) {
                int forceOneScan = Scanner.getForceOneLEBluetoothScan(appContext);

                if (Event.getGlobalEventsRunning(appContext) || (forceOneScan == Scanner.FORCE_ONE_SCAN_FROM_PREF_DIALOG))
                {

                    boolean scanStarted = (BluetoothScanJob.getWaitForLEResults(appContext));

                    if (scanStarted)
                    {
                        PPApplication.logE("@@@ BluetoothService.doWakefulWork","BluetoothLEScanBroadcastReceiver: xxx");

                        BluetoothScanJob.fillBoundedDevicesList(appContext);

                        BluetoothScanJob.setWaitForLEResults(appContext, false);

                        Scanner.setForceOneLEBluetoothScan(appContext, Scanner.FORCE_ONE_SCAN_DISABLED);

                        if (forceOneScan != Scanner.FORCE_ONE_SCAN_FROM_PREF_DIALOG)// not start service for force scan
                        {
                            // start job
                            final Context _context = appContext;
                            new Handler(appContext.getMainLooper()).postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    EventsHandlerJob.startForSensor(EventsHandler.SENSOR_TYPE_BLUETOOTH_SCANNER);
                                }
                            }, 5000);
                        }
                    }
                }
            }
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
                    PPApplication.logE("BluetoothService.getConnectedDevices","BluetoothConnectionBroadcastReceiver: device.name="+device.name);
                    PPApplication.logE("BluetoothService.getConnectedDevices", "BluetoothConnectionBroadcastReceiver: device.timestamp="+sdf.format(device.timestamp));
                    //long bootTime = System.currentTimeMillis() - SystemClock.elapsedRealtime() - gmtOffset;
                    Calendar calendar = Calendar.getInstance();
                    long bootTime = calendar.getTimeInMillis() - SystemClock.elapsedRealtime() - gmtOffset;
                    PPApplication.logE("BluetoothService.getConnectedDevices", "BluetoothConnectionBroadcastReceiver: bootTime="+sdf.format(bootTime));

                    if (device.timestamp >= bootTime) {
                        PPApplication.logE("BluetoothService.getConnectedDevices", "BluetoothConnectionBroadcastReceiver: added");
                        connectedDevices.add(device);
                    }
                    else
                        PPApplication.logE("BluetoothService.getConnectedDevices", "BluetoothConnectionBroadcastReceiver: not added");
                }
            }

            PPApplication.logE("BluetoothService.getConnectedDevices", "BluetoothConnectionBroadcastReceiver: connectedDevices.size()=" + connectedDevices.size());
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

            boolean found = false;
            for (BluetoothDeviceData _device : connectedDevices) {
                if (_device.address.equals(device.getAddress())) {
                    found = true;
                    break;
                }
            }
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

    static void clearConnectedDevices(Context context, boolean onlyOld)
    {
        PPApplication.logE("BluetoothService.clearConnectedDevices","BluetoothConnectionBroadcastReceiver: onlyOld="+onlyOld);

        if (onlyOld) {
            getConnectedDevices(context);
        }

        synchronized (PPApplication.bluetoothConnectionChangeStateMutex) {
            if (connectedDevices != null) {
                if (onlyOld) {
                    int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();
                    for (BluetoothDeviceData device : connectedDevices) {
                        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                        PPApplication.logE("BluetoothService.clearConnectedDevices","BluetoothConnectionBroadcastReceiver: device.name="+device.name);
                        PPApplication.logE("BluetoothService.clearConnectedDevices", "BluetoothConnectionBroadcastReceiver: device.timestamp="+sdf.format(device.timestamp));
                        //long bootTime = System.currentTimeMillis() - SystemClock.elapsedRealtime() - gmtOffset;
                        Calendar calendar = Calendar.getInstance();
                        long bootTime = calendar.getTimeInMillis() - SystemClock.elapsedRealtime() - gmtOffset;
                        PPApplication.logE("BluetoothService.clearConnectedDevices", "BluetoothConnectionBroadcastReceiver: bootTime="+sdf.format(bootTime));
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

    public static boolean isBluetoothConnected(Context context, String adapterName)
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

    /*
    public static boolean isAdapterNameScanned(DataWrapper dataWrapper, int connectionType)
    {
        if (isBluetoothConnected(dataWrapper.context, ""))
        {
            synchronized (PPApplication.bluetoothConnectionChangeStateMutex) {
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
    */

}
