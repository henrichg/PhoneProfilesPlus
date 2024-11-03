package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ListView;

import androidx.appcompat.app.AlertDialog;

class MultiSelectListDialog
{
    final AlertDialog mDialog;
    final Activity activity;

    /** @noinspection FieldCanBeLocal*/
    private final ListView listView;

    final int itemsRes;
    final boolean[] itemValues;

    MultiSelectListDialog(int _titleRes, int _itemsRes, boolean[] _itemValues,
                          DialogInterface.OnClickListener _positiveButtonClick,
                          Activity _activity)
    {
        this.activity = _activity;
        this.itemsRes = _itemsRes;
        this.itemValues = _itemValues;

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        dialogBuilder.setTitle(activity.getString(_titleRes));
        dialogBuilder.setCancelable(true);
        dialogBuilder.setNegativeButton(android.R.string.cancel, null);

        LayoutInflater inflater = activity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_pp_list_preference, null);
        dialogBuilder.setView(layout);

        dialogBuilder.setPositiveButton(android.R.string.ok, _positiveButtonClick);

        mDialog = dialogBuilder.create();

        mDialog.setOnShowListener(dialog -> doShow());

        listView = layout.findViewById(R.id.pp_list_pref_dlg_listview);
        // moved from doShow(), better for dialog animation and
        // also correct the displacement of the dialog
        if (listView != null) {
            MultiSelectListDialogAdapter listAdapter = new MultiSelectListDialogAdapter(itemsRes, this);
            listView.setAdapter(listAdapter);
        }

        //noinspection DataFlowIssue
        listView.setOnItemClickListener((parent, item, position, id) -> {
            CheckBox chb = item.findViewById(R.id.pp_multiselect_list_pref_dlg_item_checkbox);
            itemValues[position] = !itemValues[position];
            //noinspection DataFlowIssue
            chb.setChecked(itemValues[position]);
        });
    }

    private void doShow() {
//        MultiSelectListDialogAdapter listAdapter = new MultiSelectListDialogAdapter(itemsRes, this);
//        listView.setAdapter(listAdapter);
    }

    void show() {
        if (!activity.isFinishing())
            mDialog.show();
    }

}
