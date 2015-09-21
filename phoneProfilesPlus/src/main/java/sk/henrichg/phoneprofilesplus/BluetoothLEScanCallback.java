package sk.henrichg.phoneprofilesplus;

import android.util.Log;

import java.util.List;

import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanResult;

public class BluetoothLEScanCallback extends ScanCallback {
    @Override
    public void onScanResult(int callbackType, ScanResult result) {
        GlobalData.logE("BluetoothLEScanCallback", "onScanResult - callbackType=" + callbackType);
        GlobalData.logE("BluetoothLEScanCallback", "onScanResult - result=" + result.toString());
    }

    @Override
    public void onBatchScanResults(List<ScanResult> results) {
        for (ScanResult result : results) {
            GlobalData.logE("BluetoothLEScanCallback", "onBatchScanResults - result=" + result.toString());
        }

    }

    @Override
    public void onScanFailed(int errorCode) {
        GlobalData.logE("BluetoothLEScanCallback", "onScanFailed - errorCode=" + errorCode);
    }
}
