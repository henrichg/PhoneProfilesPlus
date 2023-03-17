package sk.henrichg.phoneprofilesplus;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;

class BluetoothLEScanner {

    //private final Context context;

    final BluetoothAdapter bluetooth;
    final BluetoothLeScanner bluetoothLeScanner;
    final BluetoothLEScanCallback bluetoothLEScanCallback21;

    BluetoothLEScanner(Context _context) {
        //context = _context;

        bluetooth = BluetoothAdapter.getDefaultAdapter(); //getBluetoothAdapter(context);
        bluetoothLeScanner = bluetooth.getBluetoothLeScanner();
        bluetoothLEScanCallback21 = new BluetoothLEScanCallback(_context);
    }

}
