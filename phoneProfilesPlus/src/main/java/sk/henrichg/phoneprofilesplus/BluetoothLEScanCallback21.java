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

    private Context context;

    BluetoothLEScanCallback21(Context _context) {
        context = _context;
    }

    public void onScanResult(int callbackType, ScanResult result) {
        boolean scanStarted = (BluetoothScanAlarmBroadcastReceiver.getWaitForLEResults(context));

        if (scanStarted) {
            //GlobalData.logE("BluetoothLEScanCallback21", "onScanResult - callbackType=" + callbackType);
            //GlobalData.logE("BluetoothLEScanCallback21", "onScanResult - result=" + result.toString());

            BluetoothDevice device = result.getDevice();
            String btName = device.getName();
            GlobalData.logE("BluetoothLEScanCallback21", "onScanResult - deviceName=" + btName);

            BluetoothDeviceData deviceData = new BluetoothDeviceData(btName, device.getAddress(),
                    BluetoothScanAlarmBroadcastReceiver.getBluetoothType(device), false);

            BluetoothScanAlarmBroadcastReceiver.addScanResult(context, deviceData);
        }
    }

    public void onBatchScanResults(List<ScanResult> results) {
        boolean scanStarted = (BluetoothScanAlarmBroadcastReceiver.getWaitForLEResults(context));

        if (scanStarted) {
            for (ScanResult result : results) {
                //GlobalData.logE("BluetoothLEScanCallback21", "onBatchScanResults - result=" + result.toString());

                BluetoothDevice device = result.getDevice();
                String btName = device.getName();
                GlobalData.logE("BluetoothLEScanCallback21", "onBatchScanResults - deviceName=" + btName);

                BluetoothDeviceData deviceData = new BluetoothDeviceData(btName, device.getAddress(),
                        BluetoothScanAlarmBroadcastReceiver.getBluetoothType(device), false);

                BluetoothScanAlarmBroadcastReceiver.addScanResult(context, deviceData);
            }
        }
    }

    public void onScanFailed(int errorCode) {
        GlobalData.logE("BluetoothLEScanCallback21", "onScanFailed - errorCode=" + errorCode);
    }

}
