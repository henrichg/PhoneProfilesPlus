package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

class ApplicationEditorDialogViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private ImageView imageViewIcon;
    private TextView textViewAppName;
    private RadioButton radioBtn;
    private TextView textViewAppType;

    private final Context context;
    private final ApplicationEditorDialog dialog;

    private Application application;

    ApplicationEditorDialogViewHolder(View itemView, Context context, ApplicationEditorDialog d)
    {
        super(itemView);

        this.context = context;
        this.dialog = d;

        imageViewIcon = itemView.findViewById(R.id.applications_editor_dialog_item_icon);
        textViewAppName = itemView.findViewById(R.id.applications_editor_dialog_item_app_name);
        radioBtn = itemView.findViewById(R.id.applications_editor_dialog_item_radiobutton);
        textViewAppType = itemView.findViewById(R.id.applications_editor_dialog_item_app_type);

        radioBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                RadioButton rb = (RadioButton) v;
                rb.setChecked(true);
                dialog.doOnItemSelected((Integer)rb.getTag());
            }
        });

        itemView.setOnClickListener(this);
    }

    @SuppressLint("SetTextI18n")
    void bindApplication(Application application, int position) {
        this.application = application;

        // Display Application data
        imageViewIcon.setImageBitmap(EditorProfilesActivity.getApplicationsCache().getApplicationIcon(application, false));
        textViewAppName.setText(application.appLabel);
        if (application.shortcut)
            textViewAppType.setText("- "+context.getString(R.string.applications_preference_applicationType_shortcut));
        else
            textViewAppType.setText("- "+context.getString(R.string.applications_preference_applicationType_application));

        if (dialog.selectedPosition == position)
            radioBtn.setChecked(true);
        else
            radioBtn.setChecked(false);
        radioBtn.setTag(position);
    }

    @Override
    public void onClick(View v) {
        int position = dialog.cachedApplicationList.indexOf(application);
        dialog.doOnItemSelected(position);
        radioBtn.setChecked(true);
    }

}
