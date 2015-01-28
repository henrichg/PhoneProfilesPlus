package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.Context;
import android.content.Intent;

@SuppressWarnings("deprecation")
public class Keyguard {

	public static Intent keyguardService = null;
	private static KeyguardManager keyguardManager = null;
	private static KeyguardLock keyguardLock = null;
	
	static final String KEYGUARD_LOCK = "phoneProfilesPlus.keyguardLock";

	public static void initialize(Context context)
	{
	    if (keyguardManager == null)
	    	keyguardManager = (KeyguardManager)context.getSystemService(Activity.KEYGUARD_SERVICE);
	    if (keyguardLock == null)
	    	keyguardLock = keyguardManager.newKeyguardLock(KEYGUARD_LOCK);
	}
	
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
	
	public static void disable()
	{
		if (keyguardLock != null)
			keyguardLock.disableKeyguard();
	}
	
	public static void reenable()
	{
		if (keyguardLock != null)
			keyguardLock.reenableKeyguard();
	}
	
}
