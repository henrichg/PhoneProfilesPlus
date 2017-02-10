package sk.henrichg.phoneprofilesplus;

import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class LockDeviceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PPApplication.lockDeviceActivity = this;

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        decorView.setSystemUiVisibility(uiOptions);

        setContentView(R.layout.activity_lock_device);

        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.screenBrightness =0.005f;
        getWindow().setAttributes(lp);

        PPApplication.screenTimeoutBeforeDeviceLock = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 15000);
        if (Permissions.checkLockDevice(getApplicationContext()))
            Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 15000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PPApplication.lockDeviceActivity = null;
        if (Permissions.checkLockDevice(getApplicationContext()))
            Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, PPApplication.screenTimeoutBeforeDeviceLock);
    }
}
