package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

public class LockDeviceActivity extends AppCompatActivity {

    private WindowManager windowManager;
    private View view = null;

    @SuppressLint("InflateParams")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PPApplication.logE("LockDeviceActivity.onCreate", "xxx");

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
        ActivateProfileHelper.screenTimeoutUnlock(getApplicationContext());
        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 1000);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.flags = 1808;
        if (android.os.Build.VERSION.SDK_INT < 26)
            //noinspection deprecation
            params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
        else
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
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

        /*
        WindowManager.LayoutParams aParams = getWindow().getAttributes();
        aParams.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        aParams.screenBrightness = 0;
        getWindow().setAttributes(aParams);
        */

        Handler handler = new Handler(getMainLooper());
        handler.postDelayed(new Runnable() {
            public void run() {
                if (PPApplication.lockDeviceActivity != null) {
                    PPApplication.logE("LockDeviceActivity.handler", "xxx");
                    PPApplication.lockDeviceActivity.finish();
                    PPApplication.lockDeviceActivity.overridePendingTransition(0, 0);
                }
            }
        }, 20000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        PPApplication.logE("LockDeviceActivity.onDestroy", "xxx");

        if (view != null)
            try {
                windowManager.removeViewImmediate(view);
            } catch (Exception ignored) {}

        PPApplication.lockDeviceActivity = null;
        if (Permissions.checkLockDevice(getApplicationContext()))
            Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, PPApplication.screenTimeoutBeforeDeviceLock);
    }

}
