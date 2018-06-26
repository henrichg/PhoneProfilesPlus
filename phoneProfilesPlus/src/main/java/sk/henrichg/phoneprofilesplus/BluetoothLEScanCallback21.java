package sk.henrichg.phoneprofilesplus;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Build;

import java.util.List;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class BluetoothLEScanCallback21 extends ScanCallback {

    private final Context context;

    BluetoothLEScanCallback21(Context _context) {
        context = _context;
    }

    public void onScanResult(int callbackType, ScanResult result) {
        CallsCounter.logCounter(context, "BluetoothLEScanCallback21.onScanResult", "BluetoothLEScanCallback21.onScanResult");

        boolean scanStarted = (BluetoothScanJob.getWaitForLEResults(context));

        if (scanStarted) {
            //PPApplication.logE("BluetoothLEScanCallback21", "onScanResult - callbackType=" + callbackType);
            //PPApplication.logE("BluetoothLEScanCallback21", "onScanResult - result=" + result.toString());

            BluetoothDevice device = result.getDevice();
            String btName = device.getName();
            PPApplication.logE("BluetoothLEScanCallback21", "onScanResult - deviceName=" + btName);

            BluetoothDeviceData deviceData = new BluetoothDeviceData(btName, device.getAddress(),
                    BluetoothScanJob.getBluetoothType(device), false, 0, false, true);

            BluetoothScanJob.addLEScanResult(deviceData);
        }
    }

    public void onBatchScanResults(List<ScanResult> results) {
        CallsCounter.logCounter(context, "BluetoothLEScanCallback21.onBatchScanResults", "BluetoothLEScanCallback21.onBatchScanResults");

        boolean scanStarted = (BluetoothScanJob.getWaitForLEResults(context));

        if (scanStarted) {
            for (ScanResult result : results) {
                //PPApplication.logE("BluetoothLEScanCallback21", "onBatchScanResults - result=" + result.toString());

                BluetoothDevice device = result.getDevice();
                String btName = device.getName();
                PPApplication.logE("BluetoothLEScanCallback21", "onBatchScanResults - deviceName=" + btName);

                BluetoothDeviceData deviceData = new BluetoothDeviceData(btName, device.getAddress(),
                        BluetoothScanJob.getBluetoothType(device), false, 0, false, true);

                BluetoothScanJob.addLEScanResult(deviceData);
            }
        }
    }

    public void onScanFailed(int errorCode) {
        PPApplication.logE("BluetoothLEScanCallback21", "onScanFailed - errorCode=" + errorCode);
    }

}
