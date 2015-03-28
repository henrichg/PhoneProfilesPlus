package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;

//getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

public class KeyguardService extends Service {

    static final String KEYGUARD_LOCK = "phoneProfilesPlus.keyguardLock";

    private KeyguardManager keyguardManager;
    private KeyguardManager.KeyguardLock keyguardLock;

    @SuppressWarnings("deprecation")
	@Override
    public void onCreate()
	{
        GlobalData.logE("$$$ KeyguardService.onStartCommand","onCreate");
        keyguardManager = (KeyguardManager)getBaseContext().getSystemService(Activity.KEYGUARD_SERVICE);
        keyguardLock = keyguardManager.newKeyguardLock(KEYGUARD_LOCK);
	}

	@Override
    public void onDestroy()
	{
        GlobalData.logE("$$$ KeyguardService.onStartCommand", "onDestroy");
        reenableKeyguard();
    }

    @SuppressWarnings("deprecation")
	@Override
    public int onStartCommand(Intent intent, int flags, int startId)
	{
		Context context = getBaseContext();

        if (!GlobalData.getApplicationStarted(context)) {
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
        if (android.os.Build.VERSION.SDK_INT >= 16)
            secureKeyguard = keyguardManager.isKeyguardSecure();
        else
		    secureKeyguard = keyguardManager.inKeyguardRestrictedInputMode();
        GlobalData.logE("$$$ KeyguardService.onStartCommand","secureKeyguard="+secureKeyguard);
        if (!secureKeyguard)
		{
            GlobalData.logE("$$$ KeyguardService.onStartCommand xxx","getLockscreenDisabled="+GlobalData.getLockscreenDisabled(context));


            if (isScreenOn) {
                GlobalData.logE("$$$ KeyguardService.onStartCommand", "screen on");

                if (GlobalData.getLockscreenDisabled(context)) {
                    GlobalData.logE("$$$ KeyguardService.onStartCommand", "Keyguard.disable(), START_STICKY");
                    reenableKeyguard();
                    disableKeyguard();
                    return START_STICKY;
                } else {
                    GlobalData.logE("$$$ KeyguardService.onStartCommand", "Keyguard.reenable(), stopSelf(), START_NOT_STICKY");
                    reenableKeyguard();
                    stopSelf();
                    return START_NOT_STICKY;
                }
            }
            /*else {
                GlobalData.logE("$$$ KeyguardService.onStartCommand", "screen off");

                if (GlobalData.getLockscreenDisabled(context)) {
                    GlobalData.logE("$$$ KeyguardService.onStartCommand", "Keyguard.disable(), START_STICKY");

                    // renable with old keyguardLock
                    Keyguard.reenable(keyguardLock);

                    // create new keyguardLock
                    //keyguardLock = keyguardManager.newKeyguardLock(KEYGUARD_LOCK);

                    stopSelf();
                    return START_NOT_STICKY;
                }
            }*/
		}

        GlobalData.logE("$$$ KeyguardService.onStartCommand"," secureKeyguard, stopSelf(), START_NOT_STICKY");
		stopSelf();
		return START_NOT_STICKY;
	}
	
	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

    public void disableKeyguard()
    {
        GlobalData.logE("$$$ Keyguard.disable","keyguardLock="+keyguardLock);
        if (keyguardLock != null)
            keyguardLock.disableKeyguard();
    }

    public void reenableKeyguard()
    {
        GlobalData.logE("$$$ Keyguard.reenable","keyguardLock="+keyguardLock);
        if (keyguardLock != null)
            keyguardLock.reenableKeyguard();
    }

}
