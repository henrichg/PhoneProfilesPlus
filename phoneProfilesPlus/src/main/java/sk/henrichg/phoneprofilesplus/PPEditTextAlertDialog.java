package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

class PPEditTextAlertDialog {
    final AlertDialog mDialog;
    final Activity activity;
    final EditText editText;

    /** @noinspection SameParameterValue*/
    PPEditTextAlertDialog(CharSequence _title, CharSequence _label,
                          String _initialEditValue,
                          CharSequence _positiveText, CharSequence _negativeText,
                          DialogInterface.OnClickListener _positiveClick,
                          DialogInterface.OnClickListener _negativeClick,
                          DialogInterface.OnCancelListener _cancelListener,
                          boolean _cancelable,
                          boolean _canceledOnTouchOutside,
                          boolean _hideButtonBarDivider,
                          Activity _activity) {
        this.activity = _activity;
        /*
        this.positiveClick = _positiveClick;
        this.negativeClick = _negativeClick;
        this.neutralClick = _neutralClick;
        */

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        dialogBuilder.setTitle(_title);
        dialogBuilder.setCancelable(true);
        //dialogBuilder.setNegativeButton(android.R.string.cancel, null);

        LayoutInflater inflater = activity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_with_edittext, null);
        dialogBuilder.setView(layout);

        dialogBuilder.setPositiveButton(_positiveText, _positiveClick);
        if (_negativeText != null)
            dialogBuilder.setNegativeButton(_negativeText, _negativeClick);

        if (_cancelListener != null)
            dialogBuilder.setOnCancelListener(_cancelListener);

        dialogBuilder.setCancelable(_cancelable);

        mDialog = dialogBuilder.create();

        //mDialog.setOnShowListener(dialog -> doShow());

        TextView messageText = layout.findViewById(R.id.dialog_with_edittext_label);
        //noinspection DataFlowIssue
        messageText.setText(_label);

        editText = layout.findViewById(R.id.dialog_with_edittext_edit);
        //noinspection DataFlowIssue
        editText.setText(_initialEditValue);
        editText.requestFocus();

        View buttonsDivider = layout.findViewById(R.id.dialog_with_edittext_buttonBarDivider);
        if (_hideButtonBarDivider)
            //noinspection DataFlowIssue
            buttonsDivider.setVisibility(View.GONE);
        else
            //noinspection DataFlowIssue
            buttonsDivider.setVisibility(View.VISIBLE);

        mDialog.setCanceledOnTouchOutside(_canceledOnTouchOutside);

    }

    /*
    private void doShow() {
        mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setVisibility(View.GONE);
        SingleSelectListDialogAdapter listAdapter = new SingleSelectListDialogAdapter(itemsRes, this);
        listView.setAdapter(listAdapter);
    }
    */

    void show() {
        if (!activity.isFinishing())
            mDialog.show();
    }

}
