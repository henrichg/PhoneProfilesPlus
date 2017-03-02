package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

public class LockDeviceActivity extends AppCompatActivity {

    WindowManager windowManager;
    View view = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PPApplication.lockDeviceActivity = this;

        /*
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        decorView.setSystemUiVisibility(uiOptions);

        setContentView(R.layout.activity_lock_device);

        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.screenBrightness =0.005f;
        getWindow().setAttributes(lp);
        */

        PPApplication.screenTimeoutBeforeDeviceLock = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 15000);
        if (Permissions.checkLockDevice(getApplicationContext())) {
            Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 15000);


            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            params.flags = 1808;
            params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
            params.gravity = Gravity.TOP;
            params.width = -1;
            params.height = -1;
            params.format = -1;
            params.screenBrightness = 0f;

            LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.activity_lock_device, null);
            view.setSystemUiVisibility(5894);
            view.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                @Override
                public void onSystemUiVisibilityChange(int i) {
                    view.setSystemUiVisibility(5894);
                }
            });

            windowManager = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
            windowManager.addView(view, params);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (view != null)
            windowManager.removeViewImmediate(view);

        PPApplication.lockDeviceActivity = null;
        if (Permissions.checkLockDevice(getApplicationContext()))
            Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, PPApplication.screenTimeoutBeforeDeviceLock);
    }

}
