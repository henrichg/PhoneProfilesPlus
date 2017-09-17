package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

class ApplicationsDialogPreferenceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    ImageView dragHandle;
    private ImageView imageViewIcon;
    private TextView textViewAppName;
    private TextView textViewAppType;
    private ImageView imageViewMenu;

    private Application application;

    private Context context;
    private DataWrapper dataWrapper;
    private ApplicationsDialogPreference preference;

    ApplicationsDialogPreferenceViewHolder(View itemView, Context context, ApplicationsDialogPreference preference)
    {
        super(itemView);

        this.context = context;
        this.preference = preference;

        dataWrapper = new DataWrapper(context, false, false, 0);

        dragHandle = itemView.findViewById(R.id.applications_pref_dlg_item_drag_handle);
        imageViewIcon = itemView.findViewById(R.id.applications_pref_dlg_item_icon);
        textViewAppName = itemView.findViewById(R.id.applications_pref_dlg_item_app_name);
        textViewAppType = itemView.findViewById(R.id.applications_pref_dlg_item_app_type);
        imageViewMenu = itemView.findViewById(R.id.applications_pref_dlg_item_edit_menu);

        itemView.setOnClickListener(this);
    }

    @SuppressLint("SetTextI18n")
    void bindApplication(Application application) {

        // 4. Bind the data to the ViewHolder
        this.application = application;

        imageViewIcon.setImageBitmap(EditorProfilesActivity.getApplicationsCache().getApplicationIcon(application, false));
        String text = application.appLabel;
        if (application.shortcutId > 0) {
            Shortcut shortcut = dataWrapper.getDatabaseHandler().getShortcut(application.shortcutId);
            if (shortcut != null)
                text = shortcut._name;
        }
        textViewAppName.setText(text);
        setTextStyle(textViewAppName, application.shortcut && (application.shortcutId == 0));
        if (application.shortcut)
            textViewAppType.setText("- "+context.getString(R.string.applications_preference_applicationType_shortcut));
        else
            textViewAppType.setText("- "+context.getString(R.string.applications_preference_applicationType_application));
        setTextStyle(textViewAppType, application.shortcut && (application.shortcutId == 0));

        imageViewMenu.setTag(application);
        final ImageView itemEditMenu = imageViewMenu;
        imageViewMenu.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                preference.showEditMenu(itemEditMenu);
            }
        });

    }

    @Override
    public void onClick(View v) {

        // 5. Handle the onClick event for the ViewHolder
        if (this.application != null) {
            preference.startEditor(this.application);
        }
    }

    private void setTextStyle(TextView textView, boolean errorColor)
    {
        if (textView != null) {
            CharSequence title = textView.getText();
            Spannable sbt = new SpannableString(title);
            Object spansToRemove[] = sbt.getSpans(0, title.length(), Object.class);
            for (Object span : spansToRemove) {
                if (span instanceof CharacterStyle)
                    sbt.removeSpan(span);
            }
            if (errorColor) {
                sbt.setSpan(new ForegroundColorSpan(Color.RED), 0, sbt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                textView.setText(sbt);
            } else {
                textView.setText(sbt);
            }
        }
    }

}
