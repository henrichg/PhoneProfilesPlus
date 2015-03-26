package sk.henrichg.phoneprofilesplus;

import android.app.KeyguardManager.KeyguardLock;

@SuppressWarnings("deprecation")
public class Keyguard {

	//public static Intent keyguardService = null;
	//private static KeyguardManager keyguardManager = null;
	//private static KeyguardLock keyguardLock = null;
	
	//static final String KEYGUARD_LOCK = "phoneProfilesPlus.keyguardLock";

    /*
	public static void initialize(Context context)
	{
        GlobalData.logE("$$$ Keyguard.initialize","keyguardManager="+keyguardManager);
        GlobalData.logE("$$$ Keyguard.initialize","keyguardLock="+keyguardLock);

        if (keyguardManager == null)
	    	keyguardManager = (KeyguardManager)context.getSystemService(Activity.KEYGUARD_SERVICE);
	    if (keyguardLock == null)
	    	keyguardLock = keyguardManager.newKeyguardLock(KEYGUARD_LOCK);
	}
	*/

    /*
	public static void destroy()
	{
		if (keyguardLock != null)
		{
			keyguardLock.reenableKeyguard();
			keyguardLock = null;
		}
		if (keyguardManager != null)
			keyguardManager = null;
	}
	*/
	
	public static void disable(KeyguardLock keyguardLock)
	{
        GlobalData.logE("$$$ Keyguard.disable","keyguardLock="+keyguardLock);
		if (keyguardLock != null)
			keyguardLock.disableKeyguard();
	}
	
	public static void reenable(KeyguardLock keyguardLock)
	{
        GlobalData.logE("$$$ Keyguard.reenable","keyguardLock="+keyguardLock);
		if (keyguardLock != null)
			keyguardLock.reenableKeyguard();
	}
	
}
