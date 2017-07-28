package sk.henrichg.phoneprofilesplus;

import android.Manifest;
import android.app.Activity;
import android.app.KeyguardManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;

//getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

public class KeyguardService extends Service {

    private KeyguardManager keyguardManager;
    @SuppressWarnings("deprecation")
    private KeyguardManager.KeyguardLock keyguardLock;

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate()
    {
        PPApplication.logE("$$$ KeyguardService.onStartCommand","onCreate");
        keyguardManager = (KeyguardManager)getBaseContext().getSystemService(Activity.KEYGUARD_SERVICE);
        keyguardLock = keyguardManager.newKeyguardLock("phoneProfilesPlus.keyguardLock");
    }

    @Override
    public void onDestroy()
    {
        PPApplication.logE("$$$ KeyguardService.onStartCommand", "onDestroy");
        reenableKeyguard();
    }

    @SuppressWarnings("deprecation")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Context context = getBaseContext();

        if (!PPApplication.getApplicationStarted(context, true)) {
            reenableKeyguard();
            stopSelf();
            return START_NOT_STICKY;
        }

        boolean isScreenOn;
        //if (android.os.Build.VERSION.SDK_INT >= 20)
        //{
        //    Display display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        //    isScreenOn = display.getState() == Display.STATE_ON;
        //}
        //else
        //{
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            isScreenOn = pm.isScreenOn();
        //}

        boolean secureKeyguard;
        secureKeyguard = keyguardManager.isKeyguardSecure();
        PPApplication.logE("$$$ KeyguardService.onStartCommand","secureKeyguard="+secureKeyguard);
        if (!secureKeyguard)
        {
            PPApplication.logE("$$$ KeyguardService.onStartCommand xxx","getLockscreenDisabled="+ ActivateProfileHelper.getLockscreenDisabled(context));


            if (isScreenOn) {
                PPApplication.logE("$$$ KeyguardService.onStartCommand", "screen on");

                if (ActivateProfileHelper.getLockscreenDisabled(context)) {
                    PPApplication.logE("$$$ KeyguardService.onStartCommand", "Keyguard.disable(), START_STICKY");
                    reenableKeyguard();
                    disableKeyguard();
                    return START_STICKY;
                } else {
                    PPApplication.logE("$$$ KeyguardService.onStartCommand", "Keyguard.reenable(), stopSelf(), START_NOT_STICKY");
                    reenableKeyguard();
                    stopSelf();
                    return START_NOT_STICKY;
                }
            }
            /*else {
                PPApplication.logE("$$$ KeyguardService.onStartCommand", "screen off");

                if (PPApplication.getLockscreenDisabled(context)) {
                    PPApplication.logE("$$$ KeyguardService.onStartCommand", "Keyguard.disable(), START_STICKY");

                    // renable with old keyguardLock
                    Keyguard.reenable(keyguardLock);

                    // create new keyguardLock
                    //keyguardLock = keyguardManager.newKeyguardLock(KEYGUARD_LOCK);

                    stopSelf();
                    return START_NOT_STICKY;
                }
            }*/
        }

        PPApplication.logE("$$$ KeyguardService.onStartCommand"," secureKeyguard, stopSelf(), START_NOT_STICKY");
        stopSelf();
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    private void disableKeyguard()
    {
        PPApplication.logE("$$$ Keyguard.disable","keyguardLock="+keyguardLock);
        if ((keyguardLock != null) && Permissions.hasPermission(getBaseContext(), Manifest.permission.DISABLE_KEYGUARD))
            keyguardLock.disableKeyguard();
    }

    private void reenableKeyguard()
    {
        PPApplication.logE("$$$ Keyguard.reenable","keyguardLock="+keyguardLock);
        if ((keyguardLock != null) && Permissions.hasPermission(getBaseContext(), Manifest.permission.DISABLE_KEYGUARD))
            keyguardLock.reenableKeyguard();
    }

}
