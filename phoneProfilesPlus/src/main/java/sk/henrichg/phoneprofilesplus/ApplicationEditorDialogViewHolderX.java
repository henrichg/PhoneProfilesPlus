package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.TooltipCompat;
import androidx.recyclerview.widget.RecyclerView;

class ApplicationEditorDialogViewHolderX extends RecyclerView.ViewHolder implements View.OnClickListener {

    private final ImageView imageViewIcon;
    private final TextView textViewAppName;
    private final RadioButton radioBtn;
    private final AppCompatImageButton imageViewMenu;

    private final ApplicationEditorDialogX dialog;

    private Application application;

    ApplicationEditorDialogViewHolderX(View itemView, /*Context context,*/ ApplicationEditorDialogX d)
    {
        super(itemView);

        this.dialog = d;

        if (dialog.selectedFilter != 2)
            imageViewIcon = itemView.findViewById(R.id.applications_editor_dialog_item_icon);
        else
            imageViewIcon = null;
        textViewAppName = itemView.findViewById(R.id.applications_editor_dialog_item_app_name);
        radioBtn = itemView.findViewById(R.id.applications_editor_dialog_item_radiobutton);
        if (dialog.selectedFilter == 2)
            imageViewMenu = itemView.findViewById(R.id.applications_editor_dlg_item_edit_menu);
        else
            imageViewMenu = null;

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
        //PPApplication.logE("ApplicationEditorDialogViewHolder.bindApplication", "this.application="+this.application);

        // Display Application data
        if (dialog.selectedFilter != 2) {
            if (EditorProfilesActivity.getApplicationsCache() != null)
                imageViewIcon.setImageBitmap(EditorProfilesActivity.getApplicationsCache().getApplicationIcon(application, false));
        }
        textViewAppName.setText(application.appLabel);

        if (dialog.selectedPosition == position)
            radioBtn.setChecked(true);
        else
            radioBtn.setChecked(false);
        radioBtn.setTag(position);

        if (imageViewMenu != null) {
            TooltipCompat.setTooltipText(imageViewMenu, dialog.activity.getString(R.string.tooltip_options_menu));
            imageViewMenu.setTag(position);
            imageViewMenu.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {
                    dialog.showEditMenu(imageViewMenu);
                }
            });
        }
    }

    @Override
    public void onClick(View v) {
        int position = dialog.applicationList.indexOf(application);
        dialog.doOnItemSelected(position);
        radioBtn.setChecked(true);
    }

}
