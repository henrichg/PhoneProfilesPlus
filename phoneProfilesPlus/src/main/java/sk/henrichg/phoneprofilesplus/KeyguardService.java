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
        if (!secureKeyguard)
		{
            // zapnutie/vypnutie lockscreenu
            //getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
            if (GlobalData.getLockscreenDisabled(context)) {
                //Keyguard.reenable();
                Keyguard.disable();
                return START_STICKY;
            }
            else {
                Keyguard.reenable();
                stopSelf();
                return START_NOT_STICKY;
            }
		}

		stopSelf();
		return START_NOT_STICKY;
	}
	
	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

}
