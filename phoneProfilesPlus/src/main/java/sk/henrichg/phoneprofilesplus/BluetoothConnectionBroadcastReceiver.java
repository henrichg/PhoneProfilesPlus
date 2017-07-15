package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.SystemClock;

import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class BluetoothConnectionBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### BluetoothConnectionBroadcastReceiver.onReceive", "xxx");

        Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(appContext, true))
            // application is not started
            return;

        Intent serviceIntent = new Intent(context, BluetoothService.class);
        serviceIntent.setAction(intent.getAction());
        serviceIntent.putExtra(BluetoothDevice.EXTRA_DEVICE, intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE));
        serviceIntent.putExtra(BluetoothDevice.EXTRA_NAME, intent.getStringExtra(BluetoothDevice.EXTRA_NAME));
        WakefulIntentService.sendWakefulWork(context, serviceIntent);
    }

}
