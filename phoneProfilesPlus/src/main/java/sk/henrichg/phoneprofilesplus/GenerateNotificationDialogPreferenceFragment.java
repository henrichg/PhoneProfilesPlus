package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceDialogFragmentCompat;

public class GenerateNotificationDialogPreferenceFragment extends PreferenceDialogFragmentCompat {

    //private Context prefContext;
    private GenerateNotificationDialogPreference preference;

    // Layout widgets
    private AlertDialog mDialog;
    private CheckBox generateChBtn = null;
    private RadioButton informationIconRBtn = null;
    private RadioButton exclamationIconRBtn = null;
    private RadioButton profileIconRBtn = null;
    private EditText notificationTitleEdtText = null;
    private EditText notificationBodyEdtText = null;

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        preference = (GenerateNotificationDialogPreference)getPreference();
        Context prefContext = preference.getContext();
        preference.fragment = this;

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(prefContext);
        dialogBuilder.setTitle(R.string.profile_preferences_generateNotification);
        dialogBuilder.setIcon(preference.getIcon());
        dialogBuilder.setCancelable(true);
        dialogBuilder.setNegativeButton(android.R.string.cancel, null);
        dialogBuilder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
            preference.generate = generateChBtn.isChecked() ? 1 : 0;
            if (informationIconRBtn.isChecked())
                preference.iconType = 0;
            else
            if (exclamationIconRBtn.isChecked())
                preference.iconType = 1;
            else
            if (profileIconRBtn.isChecked())
                preference.iconType = 2;
            else
                preference.iconType = 0;
            preference.notificationTitle = notificationTitleEdtText.getText().toString();
            preference.notificationBody = notificationBodyEdtText.getText().toString();

            preference.persistValue();
        });

        LayoutInflater inflater = ((Activity)prefContext).getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_generate_notification_preference, null);
        dialogBuilder.setView(layout);

        mDialog = dialogBuilder.create();

        mDialog.setOnShowListener(dialog -> {
//                Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                if (positive != null) positive.setAllCaps(false);
//                Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                if (negative != null) negative.setAllCaps(false);

            //preference.updateInterface(0, false);

            TextView text = layout.findViewById(R.id.generateNotificationPrefDialogIconTypeLabel);
            text.setText(getString(R.string.generate_notification_pref_dialog_icon_type)+":");
            text = layout.findViewById(R.id.generateNotificationPrefDialogNotificationTitleLabel);
            text.setText(getString(R.string.generate_notification_pref_dialog_notification_title)+":");
            text = layout.findViewById(R.id.generateNotificationPrefDialogNotificationBodyLabel);
            text.setText(getString(R.string.generate_notification_pref_dialog_notification_body)+":");

            generateChBtn.setChecked(preference.generate == 1);
            informationIconRBtn.setChecked(preference.iconType == 0);
            exclamationIconRBtn.setChecked(preference.iconType == 1);
            profileIconRBtn.setChecked(preference.iconType == 2);
            notificationTitleEdtText.setText(preference.notificationTitle);
            notificationBodyEdtText.setText(preference.notificationBody);

        });

        generateChBtn = layout.findViewById(R.id.generateNotificationPrefDialogGenerate);
        informationIconRBtn = layout.findViewById(R.id.generateNotificationPrefDialogInformationIcon);
        exclamationIconRBtn = layout.findViewById(R.id.generateNotificationPrefDialogExclamationIcon);
        profileIconRBtn = layout.findViewById(R.id.generateNotificationPrefDialogProfileIcon);

        notificationTitleEdtText = layout.findViewById(R.id.generateNotificationPrefDialogNotificationTitle);
        notificationTitleEdtText.setBackgroundTintList(ContextCompat.getColorStateList(preference._context, R.color.highlighted_spinner_all));
        notificationTitleEdtText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if ((preference.fragment != null) && (preference.fragment.getDialog() != null) && preference.fragment.getDialog().isShowing()) {
                    String value = notificationTitleEdtText.getText().toString();
                    Button okButton = mDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                    okButton.setEnabled(!value.isEmpty());
                }
            }
        });

        notificationBodyEdtText = layout.findViewById(R.id.generateNotificationPrefDialogNotificationBody);
        notificationBodyEdtText.setBackgroundTintList(ContextCompat.getColorStateList(preference._context, R.color.highlighted_spinner_all));

        return mDialog;
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if ((mDialog != null) && mDialog.isShowing())
            mDialog.dismiss();
        preference.fragment = null;
    }

}
