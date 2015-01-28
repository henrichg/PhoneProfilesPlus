package sk.henrichg.phoneprofilesplus;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class LocaleChangedReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {

		GlobalData.loadPreferences(context);
		
		if (GlobalData.applicationLanguage.equals("system"))
		{	
			NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.cancel(GlobalData.NOTIFICATION_ID);
		}
		
		//Log.d("LocaleChangedReceiver.onReceive", "xxxxx");

	}

}
