package sk.henrichg.phoneprofilesplus;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class PPDeviceAdminReceiver extends DeviceAdminReceiver {

    @Override
    public void onEnabled(Context context, Intent intent) {
        Log.e("PPDeviceAdminReceiver.onEnabled", "xxxxxxx");
        //PPApplication.showToast(context, context.getString(R.string.device_admin_receiver_status_enabled), Toast.LENGTH_SHORT);
    }

    @Override
    public CharSequence onDisableRequested(Context context, Intent intent) {
        Log.e("PPDeviceAdminReceiver.onDisableRequested", "xxxxxxx");
        //return context.getString(R.string.device_admin_receiver_status_disable_warning);
        return "";
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        Log.e("PPDeviceAdminReceiver.onDisabled", "xxxxxxx");
        //PPApplication.showToast(context, context.getString(R.string.device_admin_receiver_status_disabled), Toast.LENGTH_LONG);
    }

    /*
    @Override
    public void onPasswordChanged(Context context, Intent intent, UserHandle userHandle) {
        showToast(context, context.getString(R.string.admin_receiver_status_pw_changed));
    }
    */

}