package sk.henrichg.phoneprofilesplus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.stericson.RootShell.execution.Command;
import com.stericson.RootShell.execution.Shell;
import com.stericson.RootTools.RootTools;

import sk.henrichg.phoneprofilesplus.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class PhoneProfilesHelper {

	public static int PPHelperVersion = -1;

	public static final int PPHELPER_CURRENT_VERSION = 35;
	
	private static boolean errorNoRoot = false;
	private static boolean nowPPHelperUninstalled = false; 
	
	static public boolean isPPHelperInstalled(Context context, int minVersion)
	{
		PPHelperVersion = -1;

		if (nowPPHelperUninstalled)
			return false;
		
		// get package version
		PackageInfo pinfo = null;
		try {
			pinfo = context.getPackageManager().getPackageInfo("sk.henrichg.phoneprofileshelper", 0);
			GlobalData.logE("PhoneProfilesHelper.isPPHelperInstalled", "found");
			PPHelperVersion = pinfo.versionCode;
		} catch (NameNotFoundException e) {
			GlobalData.logE("PhoneProfilesHelper.isPPHelperInstalled", "not found");
			e.printStackTrace();
		}
		return PPHelperVersion >= minVersion;
	}
	
	/*
	static public void startPPHelper(Context context)
	{
		if (isPPHelperInstalled(context, 0))		// check PPHelper version
		{
			// start PPHelper 
			
        	//Log.e("PhoneProfilesHelper.startPPHelper","version OK");
			
			// start StartActivity
			Intent intent = new Intent("phoneprofileshelper.intent.action.START");
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			final PackageManager packageManager = context.getPackageManager();
		    List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
		    if (list.size() > 0)			
		    {
		    	context.startActivity(intent);
		    }
		    else
		    {
	        	//Log.e("PhoneProfilesHelper.startPPHelper","intent not found!");
		    }
		    
		}
		else
		{
        	//Log.e("PhoneProfilesHelper.startPPHelper","version BAD");
        }
	}
	*/
	
	private static boolean doInstallPPHelper(Activity activity)
	{
		boolean OK = true;
        errorNoRoot = false;

		/*if (!GlobalData.isRooted(false))
		{
            Log.e("PhoneProfilesHelper.doInstallPPHelper", "Device is not rooted");
            errorNoRoot = true;
			return false;
		}*/

		if (!GlobalData.grantRoot(false))
		{
            Log.e("PhoneProfilesHelper.doInstallPPHelper", "Grant root failed");
            errorNoRoot = true;
			return false;
		}
		
	    AssetManager assetManager = activity.getBaseContext().getAssets();
	    String[] files = null;
	    try {
	        files = assetManager.list("");
	    } catch (IOException e) {
	        Log.e("PhoneProfilesHelper.doInstallPPHelper", "Failed to get asset file list.", e);
	        OK = false;
	    }
	    
        //Log.e("PhoneProfilesHelper.doInstallPPHelper", "files.length="+files.length);

  		File sd = Environment.getExternalStorageDirectory();
		File exportDir = new File(sd, GlobalData.EXPORT_PATH);
		if (!(exportDir.exists() && exportDir.isDirectory()))
			exportDir.mkdirs();
	    
    	//// copy PhoneProfilesHelper.apk into sdcard
	    OK = false;
	    for(String filename : files) 
	    {
	        //Log.e("PhoneProfilesHelper.doInstallPPHelper", "filename="+filename);
	        
	        if (filename.equals("PhoneProfilesHelper.x"))
	        {
		        InputStream in = null;
		        OutputStream out = null;
		        try {
					File outFile = new File(sd, GlobalData.EXPORT_PATH + "/" + filename);
	
		        	in = assetManager.open(filename);
					out = new FileOutputStream(outFile);
					copyFile(in, out);
					in.close();
					in = null;
					out.flush();
					out.close();
					out = null;
					
					OK = true;
		        } catch(IOException e) {
		            Log.e("PhoneProfilesHelper.doInstallPPHelper", "Failed to copy asset file: " + filename, e);
		            OK = false;
		        }
		        
		        break;
	        }
	    }
	    
	    if (OK)
	    {
			//// copy PhoneProfilesHelper.apk from apk into system partition
		    OK = false;
		    
		    String sourceFile = System.getenv("EXTERNAL_STORAGE")+GlobalData.EXPORT_PATH+"/PhoneProfilesHelper.x";
		    //String sourceFile = sd+GlobalData.EXPORT_PATH+"/PhoneProfilesHelper.x";
		    String destinationFile = "PhoneProfilesHelper.apk"; 
			if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2)
			    destinationFile = "/system/priv-app/"+destinationFile; 
			else
			    destinationFile = "/system/app/"+destinationFile;
			
			//Log.e("PhoneProfilesHelper.doInstallPPHelper", "sourceFile="+sourceFile);
			//Log.e("PhoneProfilesHelper.doInstallPPHelper", "destionationFile="+destinationFile);
			

			if (GlobalData.isSELinuxEnforcing())
				Shell.defaultContext = Shell.ShellContext.RECOVERY;
			OK = RootTools.remount("/system", "RW");
			if (!OK)
				Log.e("PhoneProfilesHelper.doInstallPPHelper", "remount RW ERROR");
			if (OK)
				RootTools.deleteFileOrDirectory(destinationFile, false);
			//if (!OK)
			//	Log.e("PhoneProfilesHelper.doInstallPPHelper", "delete file ERROR");
			if (OK)
				OK = RootTools.copyFile(sourceFile, destinationFile, false, false);
			if (!OK)
				Log.e("PhoneProfilesHelper.doInstallPPHelper", "copy file ERROR");
			if (OK)
			{
				String command1 = "chmod 644 "+destinationFile;
				//if (GlobalData.isSELinuxEnforcing())
				//	command1 = GlobalData.getSELinuxEnforceCommad(command1);
				Command command = new Command(0, false, command1);
				try {
					RootTools.getShell(true, Shell.ShellContext.RECOVERY).add(command);
					OK = commandWait(command);
					OK = OK && command.getExitCode() == 0;
				} catch (Exception e) {
					e.printStackTrace();
					OK = false;
				}
			}
			if (!OK)
				Log.e("PhoneProfilesHelper.doInstallPPHelper", "chmod ERROR");
			if (OK)
				OK = RootTools.remount("/system", "RO");
			if (!OK)
				Log.e("PhoneProfilesHelper.doInstallPPHelper", "remount RO ERROR");
			//try {
			//	RootTools.closeAllShells();
			//} catch (Exception e) {
			//	e.printStackTrace();
			//}
			if (GlobalData.isSELinuxEnforcing())
				Shell.defaultContext = Shell.ShellContext.NORMAL;
			if (OK)
				Log.e("PhoneProfilesHelper.doInstallPPHelper", "PhoneProfilesHelper installed");
			else
				Log.e("PhoneProfilesHelper.doInstallPPHelper", "PhoneProfilesHelper installation failed!");

			/*
			String command1 = "mount -o remount,rw /system"; 			//mounts the system partition to be writeable
			String command2 = "rm "+destinationFile; 	    			//removes the old systemapp
			String command3 = "cp "+sourceFile+" "+destinationFile;	//copies the apk of the app to the system-apps folder	
			String command4 = "chmod 644 "+destinationFile;			//fixes the permissions
			String command5 = "mount -o remount,ro /system";			//mounts the system partition to be read-only again
			//if (GlobalData.isSELinuxEnforcing())
			//{
			//	command1 = GlobalData.getSELinuxEnforceCommand(command1, Shell.ShellContext.RECOVERY);
			//	command2 = GlobalData.getSELinuxEnforceCommand(command2, Shell.ShellContext.RECOVERY);
			//	command3 = GlobalData.getSELinuxEnforceCommand(command3, Shell.ShellContext.RECOVERY);
			//	command4 = GlobalData.getSELinuxEnforceCommand(command4, Shell.ShellContext.RECOVERY);
			//	command5 = GlobalData.getSELinuxEnforceCommand(command5, Shell.ShellContext.RECOVERY);
			//}
			Command command = new Command(0, false, command1, command2, command3, command4, command5);
			try {
				RootTools.getShell(true, Shell.ShellContext.RECOVERY).add(command);
				OK = commandWait(command);
				//RootTools.closeAllShells();
				if (OK)
					Log.e("PhoneProfilesHelper.doInstallPPHelper", "PhoneProfilesHelper installed");
				else
					Log.e("PhoneProfilesHelper.doInstallPPHelper", "PhoneProfilesHelper installation failed!");
			} catch (Exception e) {
				e.printStackTrace();
				Log.e("PhoneProfilesHelper.doInstallPPHelper", "PhoneProfilesHelper installation failed!");
				OK = false;
			}
			*/
			
			if (OK)
			{
				File file = new File(sd, GlobalData.EXPORT_PATH + "/" + "PhoneProfilesHelper.x");
				file.delete();
			}
			
	    }
	    
		return OK;
	}
	
	static public void installPPHelper(Activity activity, boolean finishActivity)
	{
		final Activity _activity = activity;
		final boolean _finishActivity = finishActivity;
		
		// set theme and language for dialog alert ;-)
		// not working on Android 2.3.x
		GUIData.setTheme(activity, true, false);
		GUIData.setLanguage(activity.getBaseContext());
		
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
		dialogBuilder.setTitle(activity.getResources().getString(R.string.phoneprofilehepler_install_title));
		dialogBuilder.setMessage(activity.getResources().getString(R.string.phoneprofilehepler_install_message));
		dialogBuilder.setPositiveButton(R.string.alert_button_yes, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				class InstallAsyncTask extends AsyncTask<Void, Integer, Boolean> 
				{
					private ProgressDialog dialog;
					
					InstallAsyncTask()
					{
				         this.dialog = new ProgressDialog(_activity);
					}
					
					@Override
					protected void onPreExecute()
					{
						super.onPreExecute();
						
					     this.dialog.setMessage(_activity.getResources().getString(R.string.phoneprofilehepler_install_title));
					     this.dialog.show();						
					}
					
					@Override
					protected Boolean doInBackground(Void... params) {
						
						boolean OK = doInstallPPHelper(_activity);
						
						return OK;
					}
					
					@Override
					protected void onPostExecute(Boolean result)
					{
						super.onPostExecute(result);
						
					    if (dialog.isShowing())
				            dialog.dismiss();
						
						if (result)
						{
					    	restartAndroid(_activity, 1, _finishActivity);
						}
						else
							installUnInstallPPhelperErrorDialog(_activity, 1, _finishActivity);
					}
					
				}
				
				new InstallAsyncTask().execute();
			}
		});
		dialogBuilder.setNegativeButton(R.string.alert_button_no, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
		    	if (_finishActivity)
		    		_activity.finish();
			}
		});
		dialogBuilder.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				if (_finishActivity)
					_activity.finish();
			}
		});
		dialogBuilder.show();
	}
	
	static private void copyFile(InputStream in, OutputStream out) throws IOException
	{
	    byte[] buffer = new byte[1024];
	    int read;
	    while((read = in.read(buffer)) != -1){
	      out.write(buffer, 0, read);
	    }
	}
	
	static private boolean doUninstallPPHelper(Activity activity)
	{
		boolean OK = false;
        errorNoRoot = false;

		/*if (!GlobalData.isRooted(false))
		{
            Log.e("PhoneProfilesHelper.doUninstallPPHelper", "Device is not rooted");
            errorNoRoot = true;
			return false;
		}*/

		if (!GlobalData.grantRoot(false))
		{
            Log.e("PhoneProfilesHelper.doUninstallPPHelper", "Grant root failed");
            errorNoRoot = true;
			return false;
		}
		
	    String destinationFile = "PhoneProfilesHelper.apk"; 
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2)
		    destinationFile = "/system/priv-app/"+destinationFile; 
		else
		    destinationFile = "/system/app/"+destinationFile;
		
		if (GlobalData.isSELinuxEnforcing())
			Shell.defaultContext = Shell.ShellContext.RECOVERY;
		//OK = RootTools.remount("/system", "RW");
		//if (!OK)
		//	Log.e("PhoneProfilesHelper.doUninstallPPHelper", "remount RW ERROR");
		//if (OK)
		Log.e("PhoneProfilesHelper.doUninstallPPHelper", "before delete file");
		RootTools.deleteFileOrDirectory(destinationFile, true);
		Log.e("PhoneProfilesHelper.doUninstallPPHelper", "after delete file");
		OK = true;
		//if (!OK)
		//	Log.e("PhoneProfilesHelper.doUninstallPPHelper", "delete file ERROR");
		//if (OK)
		//	OK = RootTools.remount("/system", "RO");
		//if (!OK)
		//	Log.e("PhoneProfilesHelper.doUninstallPPHelper", "remount RO ERROR");
		if (GlobalData.isSELinuxEnforcing())
			Shell.defaultContext = Shell.ShellContext.NORMAL;

		/*
		String command1 = "mount -o remount,rw /system";		//mounts the system partition to be writeable
		String command2 = "rm "+destinationFile;				//removes the old systemapp
		String command3 = "mount -o remount,ro /system";		//mounts the system partition to be read-only again
		//if (GlobalData.isSELinuxEnforcing())
		//{
		//	command1 = GlobalData.getSELinuxEnforceCommand(command1, Shell.ShellContext.RECOVERY);
		//	command2 = GlobalData.getSELinuxEnforceCommand(command2, Shell.ShellContext.RECOVERY);
		//	command3 = GlobalData.getSELinuxEnforceCommand(command3, Shell.ShellContext.RECOVERY);
		//}
		Command command = new Command(0, false, command1, command2, command3);
		try {
			RootTools.getShell(true, Shell.ShellContext.RECOVERY).add(command);
			OK = commandWait(command);
			//RootTools.closeAllShells();
			if (OK)
				Log.e("PhoneProfilesHelper.doUninstallPPHelper", "PhoneProfilesHelper uninstalled");
			else
				Log.e("PhoneProfilesHelper.doUninstallPPHelper", "PhoneProfilesHelper uninstallation failed!");
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("PhoneProfilesHelper.doUninstallPPHelper", "PhoneProfilesHelper uninstallation failed!");
			OK = false;
		}
		*/
		
		if (OK)
		{
			nowPPHelperUninstalled = true;
			
			Context context = activity.getApplicationContext();
			// update profiles for hardware changes
			DatabaseHandler databaseHandler = DatabaseHandler.getInstance(context);
			databaseHandler.updateForHardware(context);
			// refresh GUI
			Intent refreshIntent = new Intent();
			refreshIntent.setAction(RefreshGUIBroadcastReceiver.INTENT_REFRESH_GUI);
			context.sendBroadcast(refreshIntent);
		}
		
		return OK;
	}
	
	static public void uninstallPPHelper(Activity activity)
	{
		final Activity _activity = activity;
		
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
		dialogBuilder.setTitle(activity.getResources().getString(R.string.phoneprofilehepler_uninstall_title));
		dialogBuilder.setMessage(activity.getResources().getString(R.string.phoneprofilehepler_uninstall_message));
		dialogBuilder.setPositiveButton(R.string.alert_button_yes, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				
				class UninstallAsyncTask extends AsyncTask<Void, Integer, Boolean> 
				{
					private ProgressDialog dialog;
					
					UninstallAsyncTask()
					{
				         this.dialog = new ProgressDialog(_activity);
					}
					
					@Override
					protected void onPreExecute()
					{
						super.onPreExecute();
						
					     this.dialog.setMessage(_activity.getResources().getString(R.string.phoneprofilehepler_uninstall_title));
					     this.dialog.show();						
					}
					
					@Override
					protected Boolean doInBackground(Void... params) {
						
						boolean OK = doUninstallPPHelper(_activity);
						
						return OK;
					}
					
					@Override
					protected void onPostExecute(Boolean result)
					{
						super.onPostExecute(result);
						
					    if (dialog.isShowing())
				            dialog.dismiss();
						
						if (result)
						{
					    	restartAndroid(_activity, 2, false);
						}
						else
							installUnInstallPPhelperErrorDialog(_activity, 2, false);
					}
					
				}
				
				new UninstallAsyncTask().execute();
			}
		});
		dialogBuilder.setNegativeButton(R.string.alert_button_no, null);
		dialogBuilder.show();
	}
	
	static private void restartAndroid(Activity activity, int installUninstall, boolean finishActivity)
	{
		final Activity _activity = activity;
		final boolean _finishActivity = finishActivity;
		
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
		if (installUninstall == 1)
		{
			dialogBuilder.setTitle(activity.getResources().getString(R.string.phoneprofilehepler_reboot_title));
			dialogBuilder.setMessage(activity.getResources().getString(R.string.phoneprofilehepler_reboot_message));
		}
		else
		{
			dialogBuilder.setTitle(activity.getResources().getString(R.string.phoneprofilehepler_reboot_title_uninstall));
			dialogBuilder.setMessage(activity.getResources().getString(R.string.phoneprofilehepler_reboot_message_uninstall));
		}
		//dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
		
		dialogBuilder.setPositiveButton(R.string.alert_button_yes, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
		    	// restart device
		    	RootTools.restartAndroid();
			}
		});
		dialogBuilder.setNegativeButton(R.string.alert_button_no, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
		    	if (_finishActivity)
		    		_activity.finish();
			}
		});
		dialogBuilder.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				if (_finishActivity)
					_activity.finish();
			}
		});
		
		dialogBuilder.show();
	}
	
	static private boolean commandWait(Command cmd) throws Exception {
		boolean OK;
		
        int waitTill = 50;
        int waitTillMultiplier = 2;
        int waitTillLimit = 6400; //7 tries, 12750 msec
        //50+100+200+400+800+1600+3200+6400

        OK = true;
        
        while (!cmd.isFinished() && waitTill<=waitTillLimit) {
            synchronized (cmd) {
                try {
                    if (!cmd.isFinished()) {
                        cmd.wait(waitTill);
                        waitTill *= waitTillMultiplier;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    OK = false;
                }
            }
        }
        if (!cmd.isFinished()){
            Log.e("PhoneProfilesHelper.commandWaid", "Could not finish root command in " + (waitTill/waitTillMultiplier));
            OK = false;
        }
        
        return OK;
    }	
	
	static private void installUnInstallPPhelperErrorDialog(Activity activity, int installUninstall, boolean finishActivity)
	{
		final Activity _activity = activity;
		final boolean _finishActivity = finishActivity;
		
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
		String resString;
		if (installUninstall == 1)
			resString = activity.getResources().getString(R.string.phoneprofilehepler_install_title);
		else
			resString = activity.getResources().getString(R.string.phoneprofilehepler_uninstall_title);
		dialogBuilder.setTitle(resString);
		if (!errorNoRoot)
		{
			if (installUninstall == 1)
				resString = activity.getResources().getString(R.string.phoneprofilehepler_install_error);
			else
				resString = activity.getResources().getString(R.string.phoneprofilehepler_uninstall_error);
		}
		else
		{
			if (installUninstall == 1)
				resString = activity.getResources().getString(R.string.phoneprofilehepler_install_error_no_root);
			else
				resString = activity.getResources().getString(R.string.phoneprofilehepler_uninstall_error_no_root);
		}
		dialogBuilder.setMessage(resString + "!");
		//dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
		
		dialogBuilder.setPositiveButton(android.R.string.ok, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (_finishActivity)
					_activity.finish();
			}
		});
		dialogBuilder.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				if (_finishActivity)
					_activity.finish();
			}
		});
		
		dialogBuilder.show();
	}

	@SuppressLint("InlinedApi")
	static public void showPPHelperUpgradeNotification(Context context)
	{
		NotificationCompat.Builder mBuilder =   new NotificationCompat.Builder(context)
        	.setSmallIcon(R.drawable.ic_pphelper_upgrade_notify) // notification icon
        	.setContentTitle(context.getString(R.string.pphelper_upgrade_notification_title)) // title for notification
        	.setContentText(context.getString(R.string.pphelper_upgrade_notification_text)) // message for notification
        	.setAutoCancel(true); // clear notification after click
		Intent intent = new Intent(context, UpgradePPHelperActivity.class);
		PendingIntent pi = PendingIntent.getActivity(context, 0, intent, Intent.FLAG_ACTIVITY_NEW_TASK);
		mBuilder.setContentIntent(pi);
    	if (android.os.Build.VERSION.SDK_INT >= 16)
    		mBuilder.setPriority(Notification.PRIORITY_MAX);
    	if (android.os.Build.VERSION.SDK_INT >= 21)
    	{
    		mBuilder.setCategory(Notification.CATEGORY_RECOMMENDATION);
    		mBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
    	}
		NotificationManager mNotificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(0, mBuilder.build());		
	}
	
}
