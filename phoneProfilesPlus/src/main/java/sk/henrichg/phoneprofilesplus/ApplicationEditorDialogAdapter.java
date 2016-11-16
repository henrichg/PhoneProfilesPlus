package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

class ApplicationEditorDialogAdapter extends BaseAdapter
{
    private LayoutInflater inflater;
    private Context context;

    ApplicationEditorDialog dialog;
    Application application;
    private int selectedPosition;

    ApplicationEditorDialogAdapter(ApplicationEditorDialog dialog, Context context,
                                                Application application, int selectedPosition)
    {
        // Cache the LayoutInflate to avoid asking for a new one each time.
        inflater = LayoutInflater.from(context);
        this.context = context;

        this.dialog = dialog;
        this.application = application;
        this.selectedPosition = selectedPosition;
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

    static class ViewHolder {
        ImageView imageViewIcon;
        TextView textViewAppName;
        RadioButton radioBtn;
        TextView textViewAppType;
        int position;
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        ViewHolder holder;

        ApplicationsCache applicationsCahce = EditorProfilesActivity.getApplicationsCache();

        // Application to display
        Application application = applicationsCahce.getApplication(position, false);
        //System.out.println(String.valueOf(position));

        // Create a new row view
        if (convertView == null)
        {
            convertView = inflater.inflate(R.layout.applications_editor_dialog_list_item, parent, false);

            // Find the child views.
            holder = new ViewHolder();
            holder.imageViewIcon = (ImageView) convertView.findViewById(R.id.applications_editor_dialog_item_icon);
            holder.textViewAppName = (TextView) convertView.findViewById(R.id.applications_editor_dialog_item_app_name);
            holder.radioBtn = (RadioButton) convertView.findViewById(R.id.applications_editor_dialog_item_radiobutton);
            holder.textViewAppType = (TextView) convertView.findViewById(R.id.applications_editor_dialog_item_app_type);
            convertView.setTag(holder);
        }
        // Reuse existing row view
        else
        {
            // Because we use a ViewHolder, we avoid having to call
            // findViewById().
            holder = (ViewHolder)convertView.getTag();
        }

        holder.radioBtn.setTag(position);
        holder.radioBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                RadioButton rb = (RadioButton) v;
                dialog.doOnItemSelected((Integer)rb.getTag());
            }
        });

        // Display Application data
        holder.imageViewIcon.setImageDrawable(application.icon);
        holder.textViewAppName.setText(application.appLabel);
        if (application.shortcut)
            holder.textViewAppType.setText("- "+context.getString(R.string.applications_preference_applicationType_shortcut));
        else
            holder.textViewAppType.setText("- "+context.getString(R.string.applications_preference_applicationType_application));

        holder.radioBtn.setChecked(position == selectedPosition);

        return convertView;
    }

}
