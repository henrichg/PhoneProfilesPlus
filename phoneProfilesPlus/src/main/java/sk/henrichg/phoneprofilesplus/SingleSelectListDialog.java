package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

class SingleSelectListDialog
{
    final AlertDialog mDialog;
    final Activity activity;

    private final ListView listView;
    final DialogInterface.OnClickListener itemClick;

    final int itemsRes;
    int itemValue;

    static final int NOT_USE_RADIO_BUTTONS = -10;

    SingleSelectListDialog(boolean _showSubtitle, String _title, String _subtitle,
                           int _itemsRes, int _itemValue,
                           DialogInterface.OnClickListener _itemClick,
                           DialogInterface.OnCancelListener _cancelListener,
                           boolean hideButtonsDivider,
                           Activity _activity) {
        this.activity = _activity;
        this.itemsRes = _itemsRes;
        this.itemValue = _itemValue;
        this.itemClick = _itemClick;

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        if (_showSubtitle) {
            // custom dialog title
            LayoutInflater layoutInflater = LayoutInflater.from(activity);
            @SuppressLint("InflateParams")
            View titleView = layoutInflater.inflate(R.layout.custom_dialog_title_wtih_subtitle, null);
            TextView titleText = titleView.findViewById(R.id.custom_dialog_title);
            //noinspection DataFlowIssue
            titleText.setText(_title);
            TextView subtitleText = titleView.findViewById(R.id.custom_dialog_subtitle);
            //noinspection DataFlowIssue
            subtitleText.setText(_subtitle);
            dialogBuilder.setCustomTitle(titleView);
        } else
            dialogBuilder.setTitle(_title);

        dialogBuilder.setCancelable(true);
        dialogBuilder.setNegativeButton(android.R.string.cancel, null);

        LayoutInflater inflater = activity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_pp_list_preference, null);
        dialogBuilder.setView(layout);

        if (_cancelListener != null)
            dialogBuilder.setOnCancelListener(_cancelListener);

        View buttonsDivider = layout.findViewById(R.id.pp_list_pref_dlg_buttonBarDivider);
        if (hideButtonsDivider)
            //noinspection DataFlowIssue
            buttonsDivider.setVisibility(View.GONE);
        else
            //noinspection DataFlowIssue
            buttonsDivider.setVisibility(View.VISIBLE);

        mDialog = dialogBuilder.create();

        mDialog.setOnShowListener(dialog -> doShow());

        listView = layout.findViewById(R.id.pp_list_pref_dlg_listview);

        //noinspection DataFlowIssue
        listView.setOnItemClickListener((parent, item, position, id) -> {
            if (itemValue != NOT_USE_RADIO_BUTTONS) {
                RadioButton rb = item.findViewById(R.id.pp_list_pref_dlg_item_radiobutton);
                itemValue = position;
                //noinspection DataFlowIssue
                rb.setChecked(true);
            }
            itemClick.onClick(mDialog, position);
            mDialog.dismiss();
        });
    }

    private void doShow() {
        mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setVisibility(View.GONE);
        SingleSelectListDialogAdapter listAdapter = new SingleSelectListDialogAdapter(itemsRes, this);
        listView.setAdapter(listAdapter);
    }

    void show() {
        if (!activity.isFinishing())
            mDialog.show();
    }

}
