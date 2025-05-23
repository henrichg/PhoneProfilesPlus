package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

// added support for click to message links
// supported is all from InfoDialogPreferencesFragment.onLinkClickedListener()
public class PPAlertDialog extends DialogFragment
        implements PPLinkMovementMethod.OnPPLinkMovementMethodListener {
    private AlertDialog mDialog;
    private AppCompatActivity activity;

    private CharSequence title;
    private CharSequence message;
    private CharSequence positiveText;
    private CharSequence negativeText;
    private CharSequence neutralText;
    private CharSequence checkBoxText;
    private DialogInterface.OnClickListener positiveClick;
    private DialogInterface.OnClickListener negativeClick;
    private DialogInterface.OnClickListener neutralClick;
    private DialogInterface.OnCancelListener cancelListener;
    private DialogInterface.OnDismissListener dismissListener;
    private CompoundButton.OnCheckedChangeListener checkBoxListener;
    private boolean cancelable;
    private boolean canceledOnTouchOutside;
    private boolean checBoxChecked;
    private boolean checkBoxEnabled;
    private boolean hideButtonBarDivider;
    private boolean cancelDialogAtLinkClick;

    public PPAlertDialog() {
    }

    public PPAlertDialog(CharSequence _title, CharSequence _message,
                  CharSequence _positiveText, CharSequence _negativeText,
                  CharSequence _neutralText,
                  CharSequence _checkBoxText,
                  DialogInterface.OnClickListener _positiveClick,
                  DialogInterface.OnClickListener _negativeClick,
                  DialogInterface.OnClickListener _neutralClick,
                  DialogInterface.OnCancelListener _cancelListener,
                  DialogInterface.OnDismissListener _dismissListener,
                  CompoundButton.OnCheckedChangeListener _checkBoxListener,
                  boolean _cancelable,
                  boolean _canceledOnTouchOutside,
                  boolean _checBoxChecked,
                  boolean _checkBoxEnabled,
                  boolean _hideButtonBarDivider,
                  boolean _cancelDialogAtLinkClick,
                  AppCompatActivity _activity) {
        this.activity = _activity;
        this.title = _title;
        this.message = _message;
        this.positiveText = _positiveText;
        this.negativeText = _negativeText;
        this.neutralText = _neutralText;
        this.checkBoxText = _checkBoxText;
        this.positiveClick = _positiveClick;
        this.negativeClick = _negativeClick;
        this.neutralClick = _neutralClick;
        this.cancelListener = _cancelListener;
        this.dismissListener = _dismissListener;
        this.checkBoxListener = _checkBoxListener;
        this.cancelable = _cancelable;
        this.canceledOnTouchOutside = _canceledOnTouchOutside;
        this.checBoxChecked = _checBoxChecked;
        this.checkBoxEnabled = _checkBoxEnabled;
        this.hideButtonBarDivider = _hideButtonBarDivider;
        this.cancelDialogAtLinkClick = _cancelDialogAtLinkClick;
    }

    @SuppressLint("DialogFragmentCallbacksDetector")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        this.activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            GlobalGUIRoutines.lockScreenOrientation(activity);

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
            GlobalGUIRoutines.setCustomDialogTitle(activity, dialogBuilder, false,
                    title, null);
            //dialogBuilder.setTitle(_title);
            dialogBuilder.setCancelable(true);
            //dialogBuilder.setNegativeButton(android.R.string.cancel, null);

            LayoutInflater inflater = activity.getLayoutInflater();
            View layout = inflater.inflate(R.layout.dialog_info_preference, null);
            dialogBuilder.setView(layout);

            dialogBuilder.setPositiveButton(positiveText, positiveClick);
            if (negativeText != null)
                dialogBuilder.setNegativeButton(negativeText, negativeClick);
            if (neutralText != null)
                dialogBuilder.setNeutralButton(neutralText, neutralClick);

            /*
            if (cancelListener != null)
                dialogBuilder.setOnCancelListener(cancelListener);
            if (dismissListener != null)
                dialogBuilder.setOnDismissListener(dismissListener);
            */

            dialogBuilder.setCancelable(cancelable);

            mDialog = dialogBuilder.create();

            //mDialog.setOnShowListener(dialog -> doShow());

            TextView messageText = layout.findViewById(R.id.info_pref_dialog_info_text);
            //noinspection DataFlowIssue
            messageText.setText(message);
            messageText.setMovementMethod(new PPLinkMovementMethod(this, activity));

            View buttonsDivider = layout.findViewById(R.id.info_pref_dialog_buttonBarDivider);
            if (hideButtonBarDivider)
                //noinspection DataFlowIssue
                buttonsDivider.setVisibility(View.GONE);
            else
                //noinspection DataFlowIssue
                buttonsDivider.setVisibility(View.VISIBLE);

            mDialog.setCanceledOnTouchOutside(canceledOnTouchOutside);

            if (checkBoxListener != null) {
                CheckBox checkBox = layout.findViewById(R.id.info_pref_dialog_checkBox);
                //noinspection DataFlowIssue
                checkBox.setText(checkBoxText);
                checkBox.setEnabled(checkBoxEnabled);
                checkBox.setChecked(checBoxChecked);
                checkBox.setOnCheckedChangeListener(checkBoxListener);
                checkBox.setVisibility(View.VISIBLE);
            }

        }
        return mDialog;
    }

    public void onCancel (@NonNull DialogInterface dialog) {
        super.onCancel(dialog);
        if (cancelListener != null)
            cancelListener.onCancel(dialog);
    }

    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);

        if (dismissListener != null)
            dismissListener.onDismiss(dialog);

        if (activity != null)
            GlobalGUIRoutines.unlockScreenOrientation(activity);
    }

    /*
    private void doShow() {
        mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setVisibility(View.GONE);
        SingleSelectListDialogAdapter listAdapter = new SingleSelectListDialogAdapter(itemsRes, this);
        listView.setAdapter(listAdapter);
    }
    */

    void showDialog() {
        if ((activity != null) && (!activity.isFinishing())) {
            FragmentManager manager = activity.getSupportFragmentManager();
            if (!manager.isDestroyed())
                show(manager, "PP_ALERT_DIALOG");
        }
    }

    @Override
    public void onLinkClicked(final String linkUrl, PPLinkMovementMethod.LinkType linkTypeUrl,
                              final String linkText, PPLinkMovementMethod.LinkType linkTypeText) {
        if (cancelDialogAtLinkClick)
            mDialog.cancel();

        InfoDialogPreferenceFragment.onLinkClickedListener(linkUrl, linkTypeUrl, linkText, linkTypeText,
                            title, activity, activity);
    }

    @Override
    public void onLongClick(String text) {

    }

}
