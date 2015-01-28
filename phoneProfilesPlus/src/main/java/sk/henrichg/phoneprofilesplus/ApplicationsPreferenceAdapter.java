package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

public class ApplicationsPreferenceAdapter extends BaseAdapter {

	//private Context context;
	
	ApplicationsPreferenceDialog dialog = null;
	private LayoutInflater inflater = null;
	String packageName;
	
	public ApplicationsPreferenceAdapter(ApplicationsPreferenceDialog dialog, Context c, String packageName)
	{
		//context = c;
		this.dialog = dialog;
		inflater = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.packageName = packageName;
	}
	
	public int getCount() {
		return EditorProfilesActivity.getApplicationsCache().getLength();
	}

	public Object getItem(int position) {
		return EditorProfilesActivity.getApplicationsCache().getPackageName(position);
	}

	public long getItemId(int position) {
		return position;
	}

	static class ViewHolder {
		  ImageView applicationIcon;
		  TextView applicationLabel;
		  RadioButton radioBtn;
		  int position;
		}
	
	public View getView(int position, View convertView, ViewGroup parent)
	{
		ViewHolder holder;
		
		View vi = convertView;
		if (convertView == null)
		{
      		vi = inflater.inflate(R.layout.applications_preference_list_item, parent, false);
      		holder = new ViewHolder();
  			holder.applicationIcon = (ImageView)vi.findViewById(R.id.applications_pref_dlg_item_icon);
  			holder.applicationLabel = (TextView)vi.findViewById(R.id.applications_pref_dlg_item_label);
	        holder.radioBtn = (RadioButton)vi.findViewById(R.id.applications_pref_dlg_item_radiobtn);
  			vi.setTag(holder);        
		}
		else
		{
			holder = (ViewHolder)vi.getTag();
		}
		
		//Log.d("ApplicationsPreferenceAdapter.getView", EditorProfilesActivity.getApplicationsCache().getApplicationLabel(position).toString());
		//Log.d("ApplicationsPreferenceAdapter.getView", EditorProfilesActivity.getApplicationsCache().getApplicationIcon(position).toString());
		
		ApplicationsCache applicationsCahce = EditorProfilesActivity.getApplicationsCache();
		
		holder.applicationLabel.setText(applicationsCahce.getApplicationLabel(position));

		Drawable icon = applicationsCahce.getApplicationIcon(position);
		//Resources resources = context.getResources();
		//int height = (int) resources.getDimension(android.R.dimen.app_icon_size);
		//int width = (int) resources.getDimension(android.R.dimen.app_icon_size);
		//icon.setBounds(0, 0, width, height);
		//applicationIcon.setCompoundDrawables(icon, null, null, null);
		holder.applicationIcon.setImageDrawable(icon);
		
		holder.radioBtn.setTag(position);
    	holder.radioBtn.setChecked(applicationsCahce.getPackageName(position).equals(packageName));
    	holder.radioBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	RadioButton rb = (RadioButton) v;
            	dialog.doOnItemSelected((Integer)rb.getTag());
            }
        });
    	
		
		return vi;
	}

	public String getApplicationPackageName(int position)
	{
		return EditorProfilesActivity.getApplicationsCache().getPackageName(position);
	}
}
