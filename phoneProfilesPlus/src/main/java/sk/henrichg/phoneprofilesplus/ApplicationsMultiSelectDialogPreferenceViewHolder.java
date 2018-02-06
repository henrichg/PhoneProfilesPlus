package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

class ApplicationsMultiSelectDialogPreferenceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private final ImageView imageViewIcon;
    private final TextView textViewAppName;
    private final CheckBox checkBox;
    private final TextView textViewAppType;

    private final Context context;

    private final boolean noShortcuts;

    private Application application;

    ApplicationsMultiSelectDialogPreferenceViewHolder(View itemView, Context context, boolean noShortcuts)
    {
        super(itemView);

        this.context = context;
        this.noShortcuts = noShortcuts;

        imageViewIcon = itemView.findViewById(R.id.applications_multiselect_pref_dlg_item_icon);
        textViewAppName = itemView.findViewById(R.id.applications_multiselect_pref_dlg_item_app_name);
        checkBox = itemView.findViewById(R.id.applications_multiselect_pref_dlg_item_checkbox);
        if (noShortcuts)
            textViewAppType = null;
        else
            textViewAppType = itemView.findViewById(R.id.applications_multiselect_pref_dlg_item_app_type);

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
        imageViewIcon.setImageBitmap(EditorProfilesActivity.getApplicationsCache().getApplicationIcon(application, noShortcuts));
        textViewAppName.setText(application.appLabel);
        if (!noShortcuts) {
            if (application.shortcut)
                textViewAppType.setText("- "+context.getString(R.string.applications_preference_applicationType_shortcut));
            else
                textViewAppType.setText("- "+context.getString(R.string.applications_preference_applicationType_application));
        }

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
