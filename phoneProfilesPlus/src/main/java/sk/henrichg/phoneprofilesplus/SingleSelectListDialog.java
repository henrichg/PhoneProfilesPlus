package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

public class SingleSelectListDialog extends DialogFragment
{
    private AlertDialog mDialog;
    private AppCompatActivity activity;

    DialogInterface.OnClickListener itemClick;
    private DialogInterface.OnCancelListener cancelListener;

    private int itemsRes;
    int itemValue;
    private boolean showSubtitle;
    private String title;
    private String subtitle;

    static final int NOT_USE_RADIO_BUTTONS = -10;

    public SingleSelectListDialog() {
    }

    public SingleSelectListDialog(boolean _showSubtitle, String _title, String _subtitle,
                           int _itemsRes, int _itemValue,
                           DialogInterface.OnClickListener _itemClick,
                           DialogInterface.OnCancelListener _cancelListener,
                           //boolean hideButtonsDivider,
                           AppCompatActivity _activity) {
        this.activity = _activity;
        this.itemsRes = _itemsRes;
        this.itemValue = _itemValue;
        this.itemClick = _itemClick;
        this.cancelListener = _cancelListener;
        this.showSubtitle = _showSubtitle;
        this.title = _title;
        this.subtitle = _subtitle;
    }

    @SuppressLint("DialogFragmentCallbacksDetector")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        this.activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            GlobalGUIRoutines.lockScreenOrientation(activity);

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
            GlobalGUIRoutines.setCustomDialogTitle(activity, dialogBuilder, showSubtitle, title, subtitle);

            dialogBuilder.setCancelable(true);
            dialogBuilder.setNegativeButton(android.R.string.cancel, null);

            LayoutInflater inflater = activity.getLayoutInflater();
            View layout = inflater.inflate(R.layout.dialog_pp_list_preference, null);
            dialogBuilder.setView(layout);

            if (cancelListener != null)
                dialogBuilder.setOnCancelListener(cancelListener);

            View buttonsDivider = layout.findViewById(R.id.pp_list_pref_dlg_buttonBarDivider);
            //if (hideButtonsDivider)
            //noinspection DataFlowIssue
            //    buttonsDivider.setVisibility(View.GONE);
            //else
            //noinspection DataFlowIssue
            buttonsDivider.setVisibility(View.VISIBLE);

            mDialog = dialogBuilder.create();

            //mDialog.setOnShowListener(dialog -> doShow());

            ListView listView = layout.findViewById(R.id.pp_list_pref_dlg_listview);
            // moved from doShow(), better for dialog animation and
            // also correct the displacement of the dialog
            if (listView != null) {
                SingleSelectListDialogAdapter listAdapter = new SingleSelectListDialogAdapter(itemsRes, this);
                listView.setAdapter(listAdapter);
            }

            //noinspection DataFlowIssue
            listView.setOnItemClickListener((parent, item, position, id) -> {
                if (itemValue != NOT_USE_RADIO_BUTTONS) {
                    RadioButton rb = item.findViewById(R.id.pp_list_pref_dlg_item_radiobutton);
                    itemValue = position;
                    //noinspection DataFlowIssue
                    rb.setChecked(true);
                }
                itemClick.onClick(mDialog, position);
                dismiss();
            });

        }
        return mDialog;
    }

    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (activity != null)
            GlobalGUIRoutines.unlockScreenOrientation(activity);
    }

//    private void doShow() {
//        mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setVisibility(View.GONE);
//        SingleSelectListDialogAdapter listAdapter = new SingleSelectListDialogAdapter(itemsRes, this);
//        listView.setAdapter(listAdapter);
//    }

    void showDialog() {
        if ((activity != null) && (!activity.isFinishing()))
            show(activity.getSupportFragmentManager(), "SINGLE_CHOICE_DIALOG");
    }

}
