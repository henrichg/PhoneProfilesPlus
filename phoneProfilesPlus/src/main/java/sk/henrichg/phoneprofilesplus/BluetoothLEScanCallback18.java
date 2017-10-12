package sk.henrichg.phoneprofilesplus;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Build;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
class BluetoothLEScanCallback18 implements BluetoothAdapter.LeScanCallback {

    private final Context context;

    BluetoothLEScanCallback18(Context _context) {
        context = _context;
    }

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        CallsCounter.logCounter(context, "BluetoothLEScanCallback18.onLeScan", "BluetoothLEScanCallback18_onLeScan");

        boolean scanStarted = (BluetoothScanJob.getWaitForLEResults(context));

        if (scanStarted) {
            //PPApplication.logE("BluetoothLEScanCallback18", "onLeScan - device=" + device.toString());

            String btName = device.getName();
            PPApplication.logE("BluetoothLEScanCallback18", "onLeScan - deviceName=" + btName);

            BluetoothDeviceData deviceData = new BluetoothDeviceData(btName, device.getAddress(),
                    BluetoothScanJob.getBluetoothType(device), false, 0);
            BluetoothScanJob.addLEScanResult(deviceData);
        }
    }

}
