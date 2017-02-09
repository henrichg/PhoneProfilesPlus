package sk.henrichg.phoneprofilesplus;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;


public class PPDeviceAdminReceiver extends DeviceAdminReceiver {

    @Override
    public void onEnabled(Context context, Intent intent) {
    }

    @Override
    public CharSequence onDisableRequested(Context context, Intent intent) {
        return context.getString(R.string.device_admin_disable_warning_ppp);
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
    }

}
