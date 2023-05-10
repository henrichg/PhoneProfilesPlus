package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class LockDeviceActivity extends AppCompatActivity
                    implements FinishLockDeviceActivityListener
{

    private View view = null;
    private boolean displayed = false;

    static private class FinishActivityBroadcastReceiver extends BroadcastReceiver {

        private final FinishLockDeviceActivityListener listener;

        public FinishActivityBroadcastReceiver(FinishLockDeviceActivityListener listener){
            this.listener = listener;
        }

        @Override
        public void onReceive( Context context, Intent intent ) {
            listener.finishActivityFromListener();
        }
    }
    private LockDeviceActivity.FinishActivityBroadcastReceiver finishActivityBroadcastReceiver;

    @SuppressLint({"WrongConstant", "InflateParams"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);

//        PPApplicationStatic.logE("[BACKGROUND_ACTIVITY] LockDeviceActivity.onCreate", "xxx");

        boolean canWriteSettings;// = true;
        //if (android.os.Build.VERSION.SDK_INT >= 23)
            canWriteSettings = Settings.System.canWrite(getApplicationContext());

        if (/*(PhoneProfilesService.getInstance() != null) &&*/ canWriteSettings) {
            //PPApplication.lockDeviceActivity = this;

            finishActivityBroadcastReceiver = new LockDeviceActivity.FinishActivityBroadcastReceiver(this);
            LocalBroadcastManager.getInstance(this).registerReceiver(finishActivityBroadcastReceiver,
                        new IntentFilter(PPApplication.PACKAGE_NAME + ".FinishLockDeviceActivityBroadcastReceiver"));
            //finishActivityBroadcastReceiver = new LockDeviceActivity.FinishActivityBroadcastReceiver(this);
            //registerReceiver(finishActivityBroadcastReceiver, new IntentFilter(
            //        PPApplication.PACKAGE_NAME + ".FinishLockDeviceActivityBroadcastReceiver"));

            PPApplication.lockDeviceActivityDisplayed = true;

            /*
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            decorView.setSystemUiVisibility(uiOptions);

            setContentView(R.layout.activity_lock_device);

            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.screenBrightness =0.005f;
            getWindow().setAttributes(lp);
            */

            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            params.flags = 1808;
            //if (android.os.Build.VERSION.SDK_INT < 26)
            //    params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
            //else
                params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            params.gravity = Gravity.TOP;
            params.width = -1;
            params.height = -1;
            params.format = -1;
            params.screenBrightness = 0f;

            //LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            //if (layoutInflater != null) {
                view = getLayoutInflater().inflate(R.layout.activity_lock_device, null);
                view.setSystemUiVisibility(5894);
                view.setOnSystemUiVisibilityChangeListener(i -> view.setSystemUiVisibility(5894));

                WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
                if (windowManager != null)
                    windowManager.addView(view, params);

                /*
                WindowManager.LayoutParams aParams = getWindow().getAttributes();
                aParams.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                aParams.screenBrightness = 0;
                getWindow().setAttributes(aParams);
                */

                displayed = true;

                PPApplication.screenTimeoutWhenLockDeviceActivityIsDisplayed = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 15000);
                //ActivateProfileHelper.removeScreenTimeoutAlwaysOnView(getApplicationContext());

                /*if (PPApplication.deviceIsOppo || PPApplication.deviceIsRealme) {
                    if (PPApplication.screenTimeoutHandler != null) {
                        PPApplication.screenTimeoutHandler.post(() -> {
                            synchronized (PPApplication.rootMutex) {
                                String command1 = "settings put system " + Settings.System.SCREEN_OFF_TIMEOUT + " 1000";
                                //if (PPApplication.isSELinuxEnforcing())
                                //	command1 = PPApplication.getSELinuxEnforceCommand(command1, Shell.ShellContext.SYSTEM_APP);
                                Command command = new Command(0, false, command1); //, command2);
                                try {
                                    RootTools.getShell(false, Shell.ShellContext.SYSTEM_APP).add(command);
                                    PPApplication.commandWait(command, "LockDeviceActivity.onCreate");
                                } catch (Exception e) {
                                    // com.stericson.rootshell.exceptions.RootDeniedException: Root Access Denied
                                    //Log.e("LockDeviceActivity.onCreate", Log.getStackTraceString(e));
                                    //PPApplicationStatic.recordException(e);
                                }
                            }
                        });
                    }
                } else*/
                Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 1000);

                LockDeviceActivityFinishBroadcastReceiver.setAlarm(getApplicationContext());
            //}
            //else
            //    finish();
        }
        else
            finish();

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        final Context appContext = getApplicationContext();

        if (displayed) {
            displayed = false;

            if (view != null)
                try {
                    WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
                    if (windowManager != null)
                        windowManager.removeViewImmediate(view);
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                }

            LockDeviceActivityFinishBroadcastReceiver.removeAlarm(appContext);

            if (Settings.System.canWrite(appContext)) {
                // restore screen timeout set before creation of this activity

                /*if (PPApplication.deviceIsOppo || PPApplication.deviceIsRealme) {
                    if (PPApplication.screenTimeoutHandler != null) {
                        PPApplication.screenTimeoutHandler.post(() -> {
                            synchronized (PPApplication.rootMutex) {
                                String command1 = "settings put system " + Settings.System.SCREEN_OFF_TIMEOUT + " " + PPApplication.screenTimeoutBeforeDeviceLock;
                                //if (PPApplication.isSELinuxEnforcing())
                                //	command1 = PPApplication.getSELinuxEnforceCommand(command1, Shell.ShellContext.SYSTEM_APP);
                                Command command = new Command(0, false, command1); //, command2);
                                try {
                                    RootTools.getShell(false, Shell.ShellContext.SYSTEM_APP).add(command);
                                    PPApplication.commandWait(command, "LockDeviceActivity.onDestroy");
                                } catch (Exception e) {
                                    // com.stericson.rootshell.exceptions.RootDeniedException: Root Access Denied
                                    //Log.e("LockDeviceActivity.onDestroy", Log.getStackTraceString(e));
                                    //PPApplicationStatic.recordException(e);
                                }
                            }
                        });
                    }
                } else*/
                if (PPApplication.screenTimeoutWhenLockDeviceActivityIsDisplayed != 0)
                    Settings.System.putInt(appContext.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, PPApplication.screenTimeoutWhenLockDeviceActivityIsDisplayed);
                else
                    Settings.System.putInt(appContext.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 30000);

                // set screen timeout from ApplicationPreferences.prefActivatedProfileScreenTimeoutWhenScreenOff
                // this replaces screen timeout set in this activity
                final int screenTimeout = ApplicationPreferences.prefActivatedProfileScreenTimeoutWhenScreenOff;
                if ((screenTimeout > 0) && (Permissions.checkScreenTimeout(appContext))) {
                    if (PPApplication.screenTimeoutHandler != null) {
                        PPApplication.screenTimeoutHandler.post(() -> ActivateProfileHelper.setScreenTimeout(screenTimeout, true, appContext));
                    }
                }
            }
            //dataWrapper.invalidateDataWrapper();
        }

        //PPApplication.lockDeviceActivity = null;
        PPApplication.lockDeviceActivityDisplayed = false;

        if (finishActivityBroadcastReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(finishActivityBroadcastReceiver);
            //unregisterReceiver(finishActivityBroadcastReceiver);
            //finishActivityBroadcastReceiver = null;
        }
    }

    @Override
    public void finish()
    {
        super.finish();
        overridePendingTransition(0, 0);
    }

    @Override
    public void finishActivityFromListener() {
        finish();
    }

}
