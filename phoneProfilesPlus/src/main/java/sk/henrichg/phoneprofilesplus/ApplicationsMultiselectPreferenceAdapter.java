package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public class ApplicationsMultiselectPreferenceAdapter extends BaseAdapter
{
    private LayoutInflater inflater;
    //private Context context;

    public ApplicationsMultiselectPreferenceAdapter(Context context)
    {
        // Cache the LayoutInflate to avoid asking for a new one each time.
        inflater = LayoutInflater.from(context);
        //this.context = context; 
    }

    public int getCount() {
        return EditorProfilesActivity.getApplicationsCache().getLength();
    }

    public Object getItem(int position) {
        return EditorProfilesActivity.getApplicationsCache().getApplication(position);
    }

    public long getItemId(int position) {
        return position;
    }
    
    public View getView(int position, View convertView, ViewGroup parent)
    {
        ApplicationsCache applicationsCahce = EditorProfilesActivity.getApplicationsCache();

        // Application to display
        Application application = applicationsCahce.getApplication(position);
        //System.out.println(String.valueOf(position));

        // The child views in each row.
        ImageView imageViewIcon;
        TextView textViewAppName;
        CheckBox checkBox;

        // Create a new row view
        if (convertView == null)
        {
            convertView = inflater.inflate(R.layout.applications_multiselect_preference_list_item, parent, false);

            // Find the child views.
            imageViewIcon = (ImageView) convertView.findViewById(R.id.applications_multiselect_pref_dlg_item_icon);
            textViewAppName = (TextView) convertView.findViewById(R.id.applications_multiselect_pref_dlg_item_app_name);
            checkBox = (CheckBox) convertView.findViewById(R.id.applications_multiselect_pref_dlg_item_checkbox);

            // Optimization: Tag the row with it's child views, so we don't
            // have to
            // call findViewById() later when we reuse the row.
            convertView.setTag(new ApplicationViewHolder(imageViewIcon, textViewAppName, checkBox));

            // If CheckBox is toggled, update the Application it is tagged with.
            checkBox.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    CheckBox cb = (CheckBox) v;
                    Application application = (Application) cb.getTag();
                    application.checked = cb.isChecked();
                }
            });
        }
        // Reuse existing row view
        else
        {
            // Because we use a ViewHolder, we avoid having to call
            // findViewById().
            ApplicationViewHolder viewHolder = (ApplicationViewHolder) convertView.getTag();
            imageViewIcon = viewHolder.imageViewIcon;
            textViewAppName = viewHolder.textViewAppName;
            checkBox = viewHolder.checkBox;
        }

        // Tag the CheckBox with the Application it is displaying, so that we
        // can
        // access the Application in onClick() when the CheckBox is toggled.
        checkBox.setTag(application);

        // Display Application data
        imageViewIcon.setImageDrawable(application.icon);
        textViewAppName.setText(application.appLabel);

        checkBox.setChecked(application.checked);

        return convertView;
    }

}
