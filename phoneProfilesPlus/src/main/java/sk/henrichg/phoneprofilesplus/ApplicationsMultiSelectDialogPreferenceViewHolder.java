package sk.henrichg.phoneprofilesplus;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

class ApplicationsMultiSelectDialogPreferenceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private final ImageView imageViewIcon;
    private final TextView textViewAppName;
    private final CheckBox checkBox;

    private CApplication application;

    //private final Context context;

    ApplicationsMultiSelectDialogPreferenceViewHolder(View itemView/*, Context context*/)
    {
        super(itemView);

        //this.context = context;

        imageViewIcon = itemView.findViewById(R.id.applications_multiselect_pref_dlg_item_icon);
        textViewAppName = itemView.findViewById(R.id.applications_multiselect_pref_dlg_item_app_name);
        checkBox = itemView.findViewById(R.id.applications_multiselect_pref_dlg_item_checkbox);

        // If CheckBox is toggled, update the Application it is tagged with.
        //noinspection DataFlowIssue
        checkBox.setOnClickListener(v -> {
            CheckBox cb = (CheckBox) v;
            CApplication application = (CApplication) cb.getTag();
            application.checked = cb.isChecked();
        });

        itemView.setOnClickListener(this);
    }

    void bindApplication(CApplication application) {
        this.application = application;

        // Display Application data
        if (PPApplicationStatic.getApplicationsCache() != null) {
            //Bitmap icon = PPApplicationStatic.getApplicationsCache().getApplicationIcon(application, true);
            //if (icon == null)
            //    PPApplicationStatic.getApplicationsCache().setApplicationIcon(context, application);
            imageViewIcon.setImageBitmap(PPApplicationStatic.getApplicationsCache().getApplicationIcon(application/*, true*/));
        }
        textViewAppName.setText(application.appLabel);

        // Tag the CheckBox with the Application it is displaying, so that we
        // can
        // access the Application in onClick() when the CheckBox is toggled.
        checkBox.setTag(application);

        checkBox.setChecked(application.checked);
    }

    @Override
    public void onClick(View v) {
        application.toggleChecked();
        checkBox.setChecked(application.checked);
    }

}
