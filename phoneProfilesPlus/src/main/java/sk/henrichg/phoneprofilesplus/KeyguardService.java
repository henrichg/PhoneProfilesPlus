package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;


public class KeyguardService extends Service {

    static final String KEYGUARD_LOCK = "phoneProfilesPlus.keyguardLock";

	@Override
    public void onCreate()
	{
	}

    @SuppressWarnings("deprecation")
	@Override
    public void onDestroy()
	{
        KeyguardManager keyguardManager = (KeyguardManager)getApplicationContext().getSystemService(Activity.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock keyguardLock = keyguardManager.newKeyguardLock(KEYGUARD_LOCK);
        Keyguard.reenable(keyguardLock);
    }

    @SuppressWarnings("deprecation")
	@Override
    public int onStartCommand(Intent intent, int flags, int startId)
	{
		Context context = getApplicationContext();

        KeyguardManager keyguardManager = (KeyguardManager)context.getSystemService(Activity.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock keyguardLock = keyguardManager.newKeyguardLock(KEYGUARD_LOCK);

        if (!GlobalData.getApplicationStarted(context)) {
            Keyguard.reenable(keyguardLock);
            stopSelf();
            return START_NOT_STICKY;
        }

        boolean secureKeyguard;
        if (android.os.Build.VERSION.SDK_INT >= 16)
            secureKeyguard = keyguardManager.isKeyguardSecure();
        else
		    secureKeyguard = keyguardManager.inKeyguardRestrictedInputMode();
        GlobalData.logE("$$$ KeyguardService.onStartCommand","secureKeyguard="+secureKeyguard);
        if (!secureKeyguard)
		{
            GlobalData.logE("$$$ KeyguardService.onStartCommand xxx","getLockscreenDisabled="+GlobalData.getLockscreenDisabled(context));
            // zapnutie/vypnutie lockscreenu
            //getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
            if (GlobalData.getLockscreenDisabled(context)) {
                GlobalData.logE("$$$ KeyguardService.onStartCommand","Keyguard.disable(), START_STICKY");
                //Keyguard.reenable(keyguardLock);
                Keyguard.disable(keyguardLock);
                return START_STICKY;
            }
            else {
                GlobalData.logE("$$$ KeyguardService.onStartCommand","Keyguard.reenable(), stopSelf(), START_NOT_STICKY");
                Keyguard.reenable(keyguardLock);
                stopSelf();
                return START_NOT_STICKY;
            }
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

}
