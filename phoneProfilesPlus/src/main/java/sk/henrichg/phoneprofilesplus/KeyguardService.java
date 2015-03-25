package sk.henrichg.phoneprofilesplus;

import android.app.KeyguardManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;


public class KeyguardService extends Service {

	@Override
    public void onCreate()
	{
		Keyguard.initialize(getApplicationContext());
	}
	 
	@Override
    public void onDestroy()
	{
    }
	 
	@Override
    public int onStartCommand(Intent intent, int flags, int startId)
	{
		Context context = getApplicationContext();

        if (!GlobalData.getApplicationStarted(context)) {
            Keyguard.reenable();
            stopSelf();
            return START_NOT_STICKY;
        }

		KeyguardManager kgMgr = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        boolean secureKeyguard;
        if (android.os.Build.VERSION.SDK_INT >= 16)
            secureKeyguard = kgMgr.isKeyguardSecure();
        else
		    secureKeyguard = kgMgr.inKeyguardRestrictedInputMode();
        GlobalData.logE("$$$ KeyguardService.onStartCommand","secureKeyguard="+secureKeyguard);
        if (!secureKeyguard)
		{
            GlobalData.logE("$$$ KeyguardService.onStartCommand xxx","getLockscreenDisabled="+GlobalData.getLockscreenDisabled(context));
            // zapnutie/vypnutie lockscreenu
            //getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
            if (GlobalData.getLockscreenDisabled(context)) {
                GlobalData.logE("$$$ KeyguardService.onStartCommand","Keyguard.disable(), START_STICKY");
                Keyguard.reenable();
                Keyguard.disable();
                return START_STICKY;
            }
            else {
                GlobalData.logE("$$$ KeyguardService.onStartCommand","Keyguard.reenable(), stopSelf(), START_NOT_STICKY");
                Keyguard.reenable();
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
