package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

class ApplicationsMultiSelectDialogPreferenceViewHolderX extends RecyclerView.ViewHolder implements View.OnClickListener {

    private final ImageView imageViewIcon;
    private final TextView textViewAppName;
    private final CheckBox checkBox;

    private Application application;

    ApplicationsMultiSelectDialogPreferenceViewHolderX(View itemView)
    {
        super(itemView);

        imageViewIcon = itemView.findViewById(R.id.applications_multiselect_pref_dlg_item_icon);
        textViewAppName = itemView.findViewById(R.id.applications_multiselect_pref_dlg_item_app_name);
        checkBox = itemView.findViewById(R.id.applications_multiselect_pref_dlg_item_checkbox);

        // If CheckBox is toggled, update the Application it is tagged with.
        checkBox.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                CheckBox cb = (CheckBox) v;
                Application application = (Application) cb.getTag();
                application.checked = cb.isChecked();
            }
        });

        itemView.setOnClickListener(this);
    }

    @SuppressLint("SetTextI18n")
    void bindApplication(Application application) {
        this.application = application;

        // Display Application data
        if (EditorProfilesActivity.getApplicationsCache() != null)
            imageViewIcon.setImageBitmap(EditorProfilesActivity.getApplicationsCache().getApplicationIcon(application, true));
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
