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

    int itemsRes;
    int itemValue;

    SingleSelectListDialog(int _titleRes, int _itemsRes, int _itemValue,
                           DialogInterface.OnClickListener _itemClick,
                           Activity _activity)
    {
        this.activity = _activity;
        this.itemsRes = _itemsRes;
        this.itemValue = _itemValue;
        this.itemClick = _itemClick;

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        dialogBuilder.setTitle(activity.getString(_titleRes));
        dialogBuilder.setCancelable(true);
        dialogBuilder.setNegativeButton(android.R.string.cancel, null);

        LayoutInflater inflater = activity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_pp_list_preference, null);
        dialogBuilder.setView(layout);

        mDialog = dialogBuilder.create();

        mDialog.setOnShowListener(dialog -> doShow());

        listView = layout.findViewById(R.id.pp_list_pref_dlg_listview);

        listView.setOnItemClickListener((parent, item, position, id) -> {
            RadioButton rb = item.findViewById(R.id.pp_list_pref_dlg_item_radiobutton);
            itemValue = position;
            rb.setChecked(true);
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
