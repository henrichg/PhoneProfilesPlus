package sk.henrichg.phoneprofilesplus;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class BluetoothLEScanCallback extends ScanCallback {

    private Context context;

    public BluetoothLEScanCallback(Context _context) {
        context = _context;
    }

    public void onScanResult(int callbackType, ScanResult result) {
        GlobalData.logE("BluetoothLEScanCallback", "onScanResult - callbackType=" + callbackType);
        GlobalData.logE("BluetoothLEScanCallback", "onScanResult - result=" + result.toString());

        BluetoothDevice device = result.getDevice();
        String btName = device.getName();

        BluetoothDeviceData deviceData = new BluetoothDeviceData(btName, device.getAddress());

        BluetoothScanAlarmBroadcastReceiver.addScanResult(context, deviceData);
    }

    public void onBatchScanResults(List<ScanResult> results) {
        for (ScanResult result : results) {
            GlobalData.logE("BluetoothLEScanCallback", "onBatchScanResults - result=" + result.toString());

            BluetoothDevice device = result.getDevice();
            String btName = device.getName();

            BluetoothDeviceData deviceData = new BluetoothDeviceData(btName, device.getAddress());

            BluetoothScanAlarmBroadcastReceiver.addScanResult(context, deviceData);
        }
    }

    public void onScanFailed(int errorCode) {
        GlobalData.logE("BluetoothLEScanCallback", "onScanFailed - errorCode=" + errorCode);
    }

}
