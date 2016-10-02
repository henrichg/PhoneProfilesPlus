package sk.henrichg.phoneprofilesplus;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Build;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BluetoothLEScanCallback18 implements BluetoothAdapter.LeScanCallback {

    private Context context;

    public BluetoothLEScanCallback18(Context _context) {
        context = _context;
    }

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {

        boolean scanStarted = (BluetoothScanAlarmBroadcastReceiver.getWaitForLEResults(context));

        if (scanStarted) {
            //GlobalData.logE("BluetoothLEScanCallback18", "onLeScan - device=" + device.toString());

            String btName = device.getName();
            GlobalData.logE("BluetoothLEScanCallback18", "onLeScan - deviceName=" + btName);

            BluetoothDeviceData deviceData = new BluetoothDeviceData(btName, device.getAddress(),
                    BluetoothScanAlarmBroadcastReceiver.getBluetoothType(device), false);
            BluetoothScanAlarmBroadcastReceiver.addScanResult(context, deviceData);
        }
    }

}
