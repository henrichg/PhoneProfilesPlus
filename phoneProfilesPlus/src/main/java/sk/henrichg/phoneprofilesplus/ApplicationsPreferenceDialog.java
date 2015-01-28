package sk.henrichg.phoneprofilesplus;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.os.AsyncTask;
//import android.preference.Preference;
//import android.preference.Preference.OnPreferenceChangeListener;
import android.view.View;
import android.content.DialogInterface.OnShowListener;
import android.content.DialogInterface.OnDismissListener;


public class ApplicationsPreferenceDialog extends Dialog 
										 implements OnShowListener,
													OnDismissListener
{

	private ApplicationsPreference applicationsPreference;
	private ApplicationsPreferenceAdapter applicationsPreferenceAdapter;
	
	private String packageName;
	
	private Context _context;
	
	private ListView listView;
	private LinearLayout linlaProgress;
	
	public ApplicationsPreferenceDialog(Context context) {
		super(context);
	}
	
	public ApplicationsPreferenceDialog(Context context, ApplicationsPreference preference, String packageName)
	{
		super(context);
		
		applicationsPreference = preference;
		this.packageName = packageName;

		_context = context;
		
		setContentView(R.layout.activity_applications_pref_dialog);
		
		linlaProgress = (LinearLayout)findViewById(R.id.applications_pref_dlg_linla_progress);
		listView = (ListView)findViewById(R.id.applications_pref_dlg_listview);
		
		applicationsPreferenceAdapter = new ApplicationsPreferenceAdapter(this, _context, packageName); 
	
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				doOnItemSelected(position);
			}

		});

/*		applicationPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference, Object newValue) {
			}	
		}); */

		setOnShowListener(this);
	}

	
	public void onShow(DialogInterface dialog) {

		if (EditorProfilesActivity.getApplicationsCache() == null)
			EditorProfilesActivity.createApplicationsCache();

		if (!EditorProfilesActivity.getApplicationsCache().isCached())
		{
			new AsyncTask<Void, Integer, Void>() {

				@Override
				protected void onPreExecute()
				{
					super.onPreExecute();
					linlaProgress.setVisibility(View.VISIBLE);
				}
				
				@Override
				protected Void doInBackground(Void... params) {
					ApplicationsCache applicationsCahce = EditorProfilesActivity.getApplicationsCache();
					applicationsCahce.getApplicationsList(_context);
					return null;
				}
				
				@Override
				protected void onPostExecute(Void result)
				{
					super.onPostExecute(result);
					
					listView.setAdapter(applicationsPreferenceAdapter);
					linlaProgress.setVisibility(View.GONE);
					ApplicationsCache applicationsCahce = EditorProfilesActivity.getApplicationsCache();
					if (applicationsCahce.isCached())
					{
						for (int position = 0; position < applicationsCahce.getLength()-1; position++)
						{
							if (applicationsCahce.getPackageName(position).equals(packageName))
							{
								listView.setSelection(position);
								listView.setItemChecked(position, true);
								listView.smoothScrollToPosition(position);
								break;
							}
						}
					}
				}
				
			}.execute();
		}
		else
		{
			listView.setAdapter(applicationsPreferenceAdapter);
			ApplicationsCache applicationsCahce = EditorProfilesActivity.getApplicationsCache();
			if (applicationsCahce.isCached())
			{
				for (int position = 0; position < applicationsCahce.getLength()-1; position++)
				{
					if (applicationsCahce.getPackageName(position).equals(packageName))
					{
						listView.setSelection(position);
						listView.setItemChecked(position, true);
						listView.smoothScrollToPosition(position);
						break;
					}
				}
			}
		}

	}
	
	public void doOnItemSelected(int position)
	{
		String packageName = applicationsPreferenceAdapter.getApplicationPackageName(position);
		applicationsPreference.setPackageName(packageName);
		ApplicationsPreferenceDialog.this.dismiss();
	}

	@Override
	public void onDismiss(DialogInterface dialog)
	{
		ApplicationsCache applicationsCahce = EditorProfilesActivity.getApplicationsCache();
		applicationsCahce.cancelCaching();
	}

}
