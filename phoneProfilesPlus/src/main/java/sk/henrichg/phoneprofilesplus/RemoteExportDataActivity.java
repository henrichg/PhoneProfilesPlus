package sk.henrichg.phoneprofilesplus;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;

public class RemoteExportDataActivity extends Activity {

	private DataWrapper dataWrapper;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//Log.e("RemoteExportDataActivity.onCreate", "xxx");
		
		dataWrapper = new DataWrapper(getBaseContext(), false, false, 0);
		
	}
	
	@Override
	protected void onStart()
	{
		super.onStart();
		exportProfiles();
	}
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();

		dataWrapper.invalidateDataWrapper();
		dataWrapper = null;
	}	
	
	
	private boolean exportApplicationPreferences(File dst) {
	    boolean res = false;
	    ObjectOutputStream output = null;
	    try {
	        output = new ObjectOutputStream(new FileOutputStream(dst));
	        SharedPreferences pref = getBaseContext().getSharedPreferences(GlobalData.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
	        output.writeObject(pref.getAll());

	        res = true;
	    } catch (FileNotFoundException e) {
	    	// this is OK
	        //e.printStackTrace();
	    	res = true;
	    } catch (IOException e) {
	        e.printStackTrace();
	    }finally {
	        try {
	            if (output != null) {
	                output.flush();
	                output.close();
	            }
	        } catch (IOException ex) {
	            ex.printStackTrace();
	        }
	    }
	    return res;
	}
	
	private void exportProfiles()
	{
		
		// set theme and language for dialog alert ;-)
		// not working on Android 2.3.x
		GUIData.setTheme(this, true, false);
		GUIData.setLanguage(getBaseContext());
		
		final Activity activity = this;
		
		class ExportAsyncTask extends AsyncTask<Void, Integer, Integer> 
		{
			
			private ProgressDialog dialog;
			
			ExportAsyncTask()
			{
		         this.dialog = new ProgressDialog(activity);
			}
			  
			@Override
			protected void onPreExecute()
			{
				super.onPreExecute();

			     this.dialog.setMessage(getResources().getString(R.string.export_profiles_alert_title));
			     this.dialog.show();						
			}
			
			@Override
			protected Integer doInBackground(Void... params) {
				int ret = dataWrapper.getDatabaseHandler().exportDB();
				if (ret == 1)
				{
					File sd = Environment.getExternalStorageDirectory();
					File exportFile = new File(sd, GlobalData.EXPORT_PATH + "/" + GUIData.EXPORT_APP_PREF_FILENAME);
					if (!exportApplicationPreferences(exportFile))
						ret = 0;
				}
				return ret;
			}
			
			@Override
			protected void onPostExecute(Integer result)
			{
				super.onPostExecute(result);
				
			    if (dialog.isShowing())
		            dialog.dismiss();
				
				//Log.e("RemoteExportDataActivity.onPostExecute","result="+result);
			    
				if (result == 1)
				{
					Intent returnIntent = new Intent();
					activity.setResult(RESULT_OK,returnIntent);
					activity.finish();
				}
				else
				{
					exportErrorDialog();
					Intent returnIntent = new Intent();
					activity.setResult(RESULT_CANCELED,returnIntent);
					activity.finish();
				}
			}
		}
		
		new ExportAsyncTask().execute();
				
	}
	
	private void exportErrorDialog()
	{
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
		dialogBuilder.setTitle(getResources().getString(R.string.export_profiles_alert_title));
		String resMessage;
		resMessage = getResources().getString(R.string.export_profiles_alert_error);
		dialogBuilder.setMessage(resMessage + "!");
		//dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
		dialogBuilder.setPositiveButton(android.R.string.ok, null);
		dialogBuilder.show();
	}
	
	
	
}
