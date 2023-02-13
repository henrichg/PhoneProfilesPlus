package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.RadioButton;

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

    SingleSelectListDialog(String _title, int _itemsRes, int _itemValue,
                           DialogInterface.OnClickListener _itemClick,
                           boolean hideButtonsDivider,
                           Activity _activity) {
        this.activity = _activity;
        this.itemsRes = _itemsRes;
        this.itemValue = _itemValue;
        this.itemClick = _itemClick;

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        dialogBuilder.setTitle(_title);
        dialogBuilder.setCancelable(true);
        dialogBuilder.setNegativeButton(android.R.string.cancel, null);

        LayoutInflater inflater = activity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_pp_list_preference, null);
        dialogBuilder.setView(layout);

        View buttonsDivider = layout.findViewById(R.id.pp_list_pref_dlg_buttonBarDivider);
        if (hideButtonsDivider)
            buttonsDivider.setVisibility(View.GONE);
        else
            buttonsDivider.setVisibility(View.VISIBLE);

        mDialog = dialogBuilder.create();

        mDialog.setOnShowListener(dialog -> doShow());

        listView = layout.findViewById(R.id.pp_list_pref_dlg_listview);

        listView.setOnItemClickListener((parent, item, position, id) -> {
            if (itemValue != NOT_USE_RADIO_BUTTONS) {
                RadioButton rb = item.findViewById(R.id.pp_list_pref_dlg_item_radiobutton);
                itemValue = position;
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
