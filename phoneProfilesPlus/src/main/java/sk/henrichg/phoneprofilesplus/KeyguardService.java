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
		
		KeyguardManager kgMgr = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
		if (!kgMgr.inKeyguardRestrictedInputMode())
		{
			DataWrapper dataWrapper = new DataWrapper(context, false, false, 0);
			Profile profile = dataWrapper.getActivatedProfile();
			profile = GlobalData.getMappedProfile(profile, context);

			if (profile != null)
			{
				// zapnutie/vypnutie lockscreenu
				//getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
				switch (profile._deviceKeyguard) {
					case 1:
						Keyguard.reenable();
						stopSelf();
						return START_NOT_STICKY;
					case 2:
						Keyguard.reenable();
						Keyguard.disable();
				        return START_STICKY;
				}
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
