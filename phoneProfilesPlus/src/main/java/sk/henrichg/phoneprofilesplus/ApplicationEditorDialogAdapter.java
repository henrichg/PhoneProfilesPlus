package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RadioButton;

import com.andraskindler.quickscroll.Scrollable;

class ApplicationEditorDialogAdapter extends BaseAdapter implements Scrollable
{
    private final LayoutInflater inflater;
    private final Context context;

    private final ApplicationEditorDialog dialog;

    ApplicationEditorDialogAdapter(ApplicationEditorDialog dialog, Context context)
    {
        // Cache the LayoutInflate to avoid asking for a new one each time.
        inflater = LayoutInflater.from(context);
        this.context = context;

        this.dialog = dialog;
    }

    public int getCount() {
        return EditorProfilesActivity.getApplicationsCache().getLength(false);
    }

    public Object getItem(int position) {
        return EditorProfilesActivity.getApplicationsCache().getApplication(position, false);
    }

    public long getItemId(int position) {
        return position;
    }

    @Override
    public String getIndicatorForPosition(int childPosition, int groupPosition) {
        Application application = (Application) getItem(childPosition);
        return application.appLabel.substring(0, 1);
    }

    @Override
    public int getScrollPosition(int childPosition, int groupPosition) {
        return childPosition;
    }

    @SuppressLint("SetTextI18n")
    public View getView(int position, View convertView, ViewGroup parent)
    {
        ApplicationEditorViewHolder holder;

        ApplicationsCache applicationsCache = EditorProfilesActivity.getApplicationsCache();

        // Application to display
        Application application = applicationsCache.getApplication(position, false);
        //System.out.println(String.valueOf(position));

        // Create a new row view
        if (convertView == null)
        {
            convertView = inflater.inflate(R.layout.applications_editor_dialog_list_item, parent, false);

            // Find the child views.
            holder = new ApplicationEditorViewHolder();
            holder.imageViewIcon = convertView.findViewById(R.id.applications_editor_dialog_item_icon);
            holder.textViewAppName = convertView.findViewById(R.id.applications_editor_dialog_item_app_name);
            holder.radioBtn = convertView.findViewById(R.id.applications_editor_dialog_item_radiobutton);
            holder.textViewAppType = convertView.findViewById(R.id.applications_editor_dialog_item_app_type);
            convertView.setTag(holder);
        }
        // Reuse existing row view
        else
        {
            // Because we use a ViewHolder, we avoid having to call
            // findViewById().
            holder = (ApplicationEditorViewHolder)convertView.getTag();
        }

        holder.radioBtn.setTag(position);
        if (dialog.selectedPosition == position)
            holder.radioBtn.setChecked(true);
        else
            holder.radioBtn.setChecked(false);
        holder.radioBtn.setTag(position);
        holder.radioBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                RadioButton rb = (RadioButton) v;
                rb.setChecked(true);
                dialog.doOnItemSelected((Integer)rb.getTag());
            }
        });

        // Display Application data
        holder.imageViewIcon.setImageBitmap(applicationsCache.getApplicationIcon(application, false));
        holder.textViewAppName.setText(application.appLabel);
        if (application.shortcut)
            holder.textViewAppType.setText("- "+context.getString(R.string.applications_preference_applicationType_shortcut));
        else
            holder.textViewAppType.setText("- "+context.getString(R.string.applications_preference_applicationType_application));

        return convertView;
    }

}
