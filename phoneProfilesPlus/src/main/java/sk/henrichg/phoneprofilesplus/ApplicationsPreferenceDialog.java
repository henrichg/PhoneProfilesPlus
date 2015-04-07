package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.afollestad.materialdialogs.MaterialDialog;

public class ApplicationsPreferenceDialog {

	private ApplicationsPreference applicationsPreference;
	private ApplicationsPreferenceAdapter applicationsPreferenceAdapter;
	
	private String packageName;
	
	private Context _context;

    private MaterialDialog mDialog;
	private ListView listView;
	private LinearLayout linlaProgress;
    private LinearLayout linlaListView;

	public ApplicationsPreferenceDialog(Context context, ApplicationsPreference preference, String packageName)
	{
		applicationsPreference = preference;
		this.packageName = packageName;

		_context = context;

        MaterialDialog.Builder dialogBuilder = new MaterialDialog.Builder(context)
                .title(R.string.title_activity_applications_preference_dialog)
                .disableDefaultFonts()
                .autoDismiss(false)
                .customView(R.layout.activity_applications_pref_dialog, false);

        dialogBuilder.showListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    ApplicationsPreferenceDialog.this.onShow(dialog);
                }
            })
            /*.cancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                }
            })*/
            .dismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    ApplicationsPreferenceDialog.this.onDismiss(dialog);
                }
            });

        mDialog = dialogBuilder.build();

		linlaProgress = (LinearLayout)mDialog.getCustomView().findViewById(R.id.applications_pref_dlg_linla_progress);
        linlaListView = (LinearLayout)mDialog.getCustomView().findViewById(R.id.applications_pref_dlg_linla_listview);
		listView = (ListView)mDialog.getCustomView().findViewById(R.id.applications_pref_dlg_listview);
		
		applicationsPreferenceAdapter = new ApplicationsPreferenceAdapter(this, _context, packageName); 
	
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				doOnItemSelected(position);
			}

		});

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
                    linlaListView.setVisibility(View.GONE);
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
                    linlaListView.setVisibility(View.VISIBLE);
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
		mDialog.dismiss();
	}

	public void onDismiss(DialogInterface dialog)
	{
		ApplicationsCache applicationsCahce = EditorProfilesActivity.getApplicationsCache();
		applicationsCahce.cancelCaching();
	}

    public void show() {
        mDialog.show();
    }

}
