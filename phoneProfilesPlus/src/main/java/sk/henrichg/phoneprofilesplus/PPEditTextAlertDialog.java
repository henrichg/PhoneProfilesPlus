package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

public class PPEditTextAlertDialog extends DialogFragment {
    private AlertDialog mDialog;
    private AppCompatActivity activity;

    private CharSequence title;
    private CharSequence label;
    private String initialEditValue;
    private CharSequence positiveText;
    private CharSequence negativeText;
    private DialogInterface.OnClickListener positiveClick;
    private DialogInterface.OnClickListener negativeClick;
    private DialogInterface.OnCancelListener cancelListener;
    private boolean cancelable;
    private boolean canceledOnTouchOutside;

    public PPEditTextAlertDialog() {
    }

    /** @noinspection SameParameterValue*/
    public PPEditTextAlertDialog(CharSequence _title, CharSequence _label,
                          String _initialEditValue,
                          CharSequence _positiveText, CharSequence _negativeText,
                          DialogInterface.OnClickListener _positiveClick,
                          DialogInterface.OnClickListener _negativeClick,
                          DialogInterface.OnCancelListener _cancelListener,
                          boolean _cancelable,
                          boolean _canceledOnTouchOutside,
                          //boolean _hideButtonBarDivider,
                          AppCompatActivity _activity) {
        this.activity = _activity;
        this.title = _title;
        this.label = _label;
        this.initialEditValue = _initialEditValue;
        this.positiveText = _positiveText;
        this.negativeText = _negativeText;
        this.positiveClick = _positiveClick;
        this.negativeClick = _negativeClick;
        this.cancelListener = _cancelListener;
        this.cancelable = _cancelable;
        this.canceledOnTouchOutside = _canceledOnTouchOutside;
    }

    @SuppressLint("DialogFragmentCallbacksDetector")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        this.activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            GlobalGUIRoutines.lockScreenOrientation(activity);

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity/*, R.style.AlertDialogStyleDayNightNarrow*/);
            GlobalGUIRoutines.setCustomDialogTitle(activity, dialogBuilder, false,
                    title, null);
            //dialogBuilder.setTitle(_title);
            dialogBuilder.setCancelable(true);
            //dialogBuilder.setNegativeButton(android.R.string.cancel, null);

            LayoutInflater inflater = activity.getLayoutInflater();
            View layout = inflater.inflate(R.layout.dialog_with_edittext, null);
            dialogBuilder.setView(layout);

            dialogBuilder.setPositiveButton(positiveText, positiveClick);
            if (negativeText != null)
                dialogBuilder.setNegativeButton(negativeText, negativeClick);

            //if (cancelListener != null)
            //    dialogBuilder.setOnCancelListener(cancelListener);

            dialogBuilder.setCancelable(cancelable);

            mDialog = dialogBuilder.create();

            //mDialog.setOnShowListener(dialog -> doShow());

            TextView messageText = layout.findViewById(R.id.dialog_with_edittext_label);
            //noinspection DataFlowIssue
            messageText.setText(label);

            EditText editText = layout.findViewById(R.id.dialog_with_edittext_edit);
            //noinspection DataFlowIssue
            editText.setText(initialEditValue);
            editText.requestFocus();

            /*
            View buttonsDivider = layout.findViewById(R.id.dialog_with_edittext_buttonBarDivider);
            if (_hideButtonBarDivider)
                //noinspection DataFlowIssue
                buttonsDivider.setVisibility(View.GONE);
            else
                //noinspection DataFlowIssue
                buttonsDivider.setVisibility(View.VISIBLE);
            */

            mDialog.setCanceledOnTouchOutside(canceledOnTouchOutside);
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
                show(manager, "PP_EDIT_TEXT_ALERT_DIALOG");
        }
    }

}
